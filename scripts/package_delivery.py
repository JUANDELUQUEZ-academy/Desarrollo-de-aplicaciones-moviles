#!/usr/bin/env python3
"""Build and verify the university source delivery from an explicit allowlist.

Run from any directory: python scripts/package_delivery.py
The PDF must already exist. Evidence and exported test reports are optional.
Only Python's standard library is required. No source files are modified.
"""

from __future__ import annotations

import fnmatch
import hashlib
import os
from pathlib import Path, PurePosixPath
import stat
import zipfile


ROOT = Path(__file__).resolve().parent.parent
OUTPUT = ROOT / "output"
ARCHIVE = OUTPUT / "RutaPaquete-proyecto.zip"
CHECKSUM = OUTPUT / "RutaPaquete-proyecto.sha256"

REQUIRED_FILES = (
    "README.md",
    ".gitignore",
    "build.gradle.kts",
    "settings.gradle.kts",
    "gradle.properties",
    "gradlew",
    "gradlew.bat",
    "gradle/libs.versions.toml",
    "gradle/wrapper/gradle-wrapper.jar",
    "gradle/wrapper/gradle-wrapper.properties",
    "app/build.gradle.kts",
)
FORBIDDEN_DIRECTORIES = {
    "build", ".gradle", ".git", ".idea", ".kotlin", "tmp", "__pycache__",
    ".venv", "venv", "node_modules",
}
FORBIDDEN_PATTERNS = (
    "local.properties", ".env", ".env.*", "*.env", "*.jks", "*.keystore",
    "*.p12", "*.pfx", "*.pem", "*.key", "*.der", "*.crt", "*.cer",
    "key.properties", "keystore.properties", "signing.properties",
    "credentials*", "secrets*", "service-account*", "service_account*",
    "google-services.json", "id_rsa*", "id_dsa*", "id_ecdsa*", "id_ed25519*",
    "*.apk", "*.aab", "*.apks", "*.dex", "*.class", "*.pyc", "*.zip",
)
SCRIPT_EXTENSIONS = {".py", ".ps1", ".sh", ".mjs", ".js", ".cmd", ".bat", ".md"}


def forbidden(path: PurePosixPath) -> bool:
    return (
        any(part.casefold() in FORBIDDEN_DIRECTORIES for part in path.parts[:-1])
        or any(fnmatch.fnmatchcase(path.name.casefold(), pattern) for pattern in FORBIDDEN_PATTERNS)
    )


def validate_archive_name(name: str) -> None:
    path = PurePosixPath(name)
    if (
        not name or "\\" in name or ":" in name or path.is_absolute()
        or any(part in {"", ".", ".."} for part in name.split("/"))
        or forbidden(path)
    ):
        raise ValueError(f"Ruta prohibida dentro del ZIP: {name!r}")
    if path.suffix.casefold() == ".pdf" and name != "entrega/RutaPaquete-informe.pdf":
        raise ValueError(f"PDF no autorizado dentro del ZIP: {name!r}")
    if path.suffix.casefold() == ".jar" and name != "gradle/wrapper/gradle-wrapper.jar":
        raise ValueError(f"JAR no autorizado dentro del ZIP: {name!r}")


def source_is_safe(source: Path) -> bool:
    """Never follow links or junctions to files outside the selected project."""
    try:
        source.relative_to(ROOT)
        source.resolve().relative_to(ROOT)
    except ValueError:
        return False
    for part in (source, *source.parents):
        # lstat also detects Windows junctions on Python versions before 3.12.
        attributes = getattr(part.lstat(), "st_file_attributes", 0)
        if part.is_symlink() or attributes & getattr(stat, "FILE_ATTRIBUTE_REPARSE_POINT", 0x400):
            return False
        if part == ROOT:
            break
    return True


def collect_files() -> dict[str, Path]:
    files: dict[str, Path] = {}

    def add(source: Path, destination: str, *, required: bool = False) -> None:
        if not source.is_file():
            if required:
                raise FileNotFoundError(f"Falta un archivo requerido: {source.relative_to(ROOT)}")
            return
        if not source_is_safe(source):
            raise ValueError(f"No se empaquetan enlaces ni rutas externas: {source}")
        relative_source = PurePosixPath(source.relative_to(ROOT).as_posix())
        if forbidden(relative_source):
            if required:
                raise ValueError(f"Archivo requerido prohibido: {relative_source}")
            return
        # No incorporar PDFs de referencia, binarios compilados ni otros archivos JAR.
        if source.suffix.casefold() == ".pdf" and destination != "entrega/RutaPaquete-informe.pdf":
            return
        if source.suffix.casefold() == ".jar" and destination != "gradle/wrapper/gradle-wrapper.jar":
            return
        validate_archive_name(destination)
        if destination in files:
            raise ValueError(f"Destino duplicado: {destination}")
        files[destination] = source

    def tree(relative: str, destination: str, *, required: bool = False, scripts: bool = False) -> None:
        directory = ROOT / relative
        if not directory.is_dir():
            if required:
                raise FileNotFoundError(f"Falta una carpeta requerida: {relative}")
            print(f"Carpeta opcional ausente: {relative}")
            return
        if not source_is_safe(directory):
            raise ValueError(f"Carpeta enlazada o externa no permitida: {relative}")
        for current, subdirs, names in os.walk(directory, followlinks=False):
            current_path = Path(current)
            subdirs[:] = sorted(
                name for name in subdirs
                if name.casefold() not in FORBIDDEN_DIRECTORIES
                and source_is_safe(current_path / name)
            )
            for name in sorted(names):
                source = current_path / name
                if scripts and source.suffix.casefold() not in SCRIPT_EXTENSIONS:
                    continue
                archive_name = (PurePosixPath(destination) / source.relative_to(directory).as_posix()).as_posix()
                add(source, archive_name)

    for relative in REQUIRED_FILES:
        add(ROOT / relative, relative, required=True)
    tree("app/src", "app/src", required=True)
    tree("app/schemas", "app/schemas", required=True)
    tree("scripts", "scripts", required=True, scripts=True)
    tree("app/keepRules", "app/keepRules")
    for pattern in ("proguard*.pro", "consumer-rules.pro", "*.keep"):
        for source in sorted((ROOT / "app").glob(pattern)):
            add(source, source.relative_to(ROOT).as_posix())
    add(ROOT / "output/pdf/RutaPaquete-informe.pdf", "entrega/RutaPaquete-informe.pdf", required=True)
    tree("output/evidencias", "entrega/evidencias")
    tree("output/pruebas", "entrega/pruebas")
    return files


def sha256_file(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def main() -> None:
    files = collect_files()
    OUTPUT.mkdir(parents=True, exist_ok=True)
    temporary = OUTPUT / "RutaPaquete-proyecto.zip.tmp"
    try:
        with zipfile.ZipFile(temporary, "w", compression=zipfile.ZIP_DEFLATED, compresslevel=6) as archive:
            for name, source in sorted(files.items()):
                # Fixed metadata makes identical inputs yield identical ZIP contents.
                info = zipfile.ZipInfo(name, date_time=(1980, 1, 1, 0, 0, 0))
                info.create_system = 3
                mode = 0o755 if name == "gradlew" or name.endswith(".sh") else 0o644
                info.external_attr = (stat.S_IFREG | mode) << 16
                info.compress_type = zipfile.ZIP_DEFLATED
                archive.writestr(info, source.read_bytes(), compress_type=zipfile.ZIP_DEFLATED, compresslevel=6)
        with zipfile.ZipFile(temporary) as archive:
            names = archive.namelist()
            if len(names) != len(set(names)) or set(names) != set(files):
                raise ValueError("El contenido final no coincide con la allowlist.")
            for name in names:
                validate_archive_name(name)
            corrupt = archive.testzip()
            if corrupt is not None:
                raise ValueError(f"El ZIP contiene un archivo corrupto: {corrupt}")
        temporary.replace(ARCHIVE)
    finally:
        if temporary.exists():
            temporary.unlink()
    checksum = sha256_file(ARCHIVE)
    CHECKSUM.write_text(f"{checksum}  {ARCHIVE.name}\n", encoding="utf-8")
    print(f"ZIP validado: {ARCHIVE}")
    print(f"Archivos: {len(files)}")
    print(f"Tamaño: {ARCHIVE.stat().st_size:,} bytes")
    print(f"SHA256: {checksum}")
    print(f"Comprobante: {CHECKSUM}")


if __name__ == "__main__":
    main()
