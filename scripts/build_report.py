#!/usr/bin/env python3
"""Create the ten-page report from real screenshots and exported test results.

Requires reportlab. Run only after output/evidencias and output/pruebas are ready.
Missing inputs are errors, never replaced by placeholders. Render and visually
inspect every output page before delivery; this script is not the visual QA step.
"""

from __future__ import annotations

from datetime import date
import json
import os
from pathlib import Path
from xml.sax.saxutils import escape

from reportlab.lib import colors
from reportlab.lib.enums import TA_LEFT
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import ParagraphStyle
from reportlab.lib.utils import ImageReader
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.pdfgen import canvas
from reportlab.platypus import Paragraph


ROOT = Path(__file__).resolve().parent.parent
DESTINATION = ROOT / "output/pdf/RutaPaquete-informe.pdf"
SUMMARY = ROOT / "output/pruebas/resumen.json"
CODE_DIRECTORY = ROOT / "output/evidencias/codigo"
APP_DIRECTORY = ROOT / "output/evidencias/app"
REGISTER_SOURCE = ROOT / "app/src/main/java/com/example/myapplication/RegisterActivity.kt"
PAGE_WIDTH, PAGE_HEIGHT = A4
MARGIN = 42
WIDTH = PAGE_WIDTH - 2 * MARGIN
TEAL = colors.HexColor("#175A63")
INK = colors.HexColor("#15383D")
MUTED = colors.HexColor("#52676A")
PALE = colors.HexColor("#F7F8F4")
LINE = colors.HexColor("#D6E3E1")
ACCENT = colors.HexColor("#A85035")
TOTAL_PAGES = 10
BODY_FONT = "Helvetica"
BOLD_FONT = "Helvetica-Bold"
CODE_FONT = "Courier"

CODE_IMAGES = ("User", "UserDao", "AppDatabase", "MainActivity", "RegisterActivity")
APP_IMAGES = (
    "01-inicio", "02a-formulario", "02-campos-obligatorios", "03-edad-menor",
    "04-contrasenas", "05-registro-exitoso", "08-rastreo",
)


def register_fonts() -> None:
    global BODY_FONT, BOLD_FONT, CODE_FONT
    fonts = Path(os.environ.get("WINDIR", "C:/Windows")) / "Fonts"
    if (fonts / "segoeui.ttf").is_file() and (fonts / "segoeuib.ttf").is_file():
        pdfmetrics.registerFont(TTFont("RutaSegoe", str(fonts / "segoeui.ttf")))
        pdfmetrics.registerFont(TTFont("RutaSegoeBold", str(fonts / "segoeuib.ttf")))
        pdfmetrics.registerFontFamily("RutaSegoe", normal="RutaSegoe", bold="RutaSegoeBold",
                                    italic="RutaSegoe", boldItalic="RutaSegoeBold")
        BODY_FONT, BOLD_FONT = "RutaSegoe", "RutaSegoeBold"
    if (fonts / "consola.ttf").is_file():
        pdfmetrics.registerFont(TTFont("RutaConsolas", str(fonts / "consola.ttf")))
        CODE_FONT = "RutaConsolas"


def load_inputs() -> tuple[dict, dict[str, ImageReader]]:
    required = [SUMMARY, REGISTER_SOURCE]
    required += [CODE_DIRECTORY / f"{name}.jpg" for name in CODE_IMAGES]
    required += [APP_DIRECTORY / f"{name}.png" for name in APP_IMAGES]
    missing = [str(path.relative_to(ROOT)) for path in required if not path.is_file()]
    if missing:
        raise FileNotFoundError("Faltan evidencias reales:\n" + "\n".join(missing))
    result = json.loads(SUMMARY.read_text(encoding="utf-8-sig"))
    for key in ("unit_tests", "instrumented_tests", "lint_errors", "lint_warnings"):
        value = result.get(key)
        if isinstance(value, bool) or not isinstance(value, int) or value < 0:
            raise ValueError(f"El resumen necesita un entero no negativo: {key}")
    if result["unit_tests"] == 0 or result["instrumented_tests"] == 0:
        raise ValueError("No se puede presentar una verificación sin pruebas registradas.")
    if not isinstance(result.get("device"), str) or not result["device"].strip():
        raise ValueError("Falta el dispositivo verificado en resumen.json.")
    date.fromisoformat(result["date"])
    if not isinstance(result.get("notes"), list) or not all(isinstance(note, str) for note in result["notes"]):
        raise ValueError("notes debe contener únicamente notas textuales verificadas.")
    images: dict[str, ImageReader] = {}
    for path in required[2:]:
        reader = ImageReader(str(path))
        width, height = reader.getSize()
        if width <= 0 or height <= 0:
            raise ValueError(f"Imagen inválida: {path}")
        images[path.stem] = reader
    return result, images


class Report:
    def __init__(self, path: Path, result: dict, images: dict[str, ImageReader]):
        self.pdf = canvas.Canvas(str(path), pagesize=A4, pageCompression=1)
        self.pdf.setTitle("RutaPaquete - Formulario de registro de su aplicación")
        self.pdf.setSubject("Implementación Kotlin, Room y evidencias reales de verificación")
        self.pdf.setAuthor("Proyecto RutaPaquete")
        self.result = result
        self.images = images
        self.page = 0

    def paragraph(self, text: str, x: float, top: float, width: float = WIDTH,
                  size: float = 10, leading: float | None = None,
                  color=INK, bold: bool = False, floor: float = 58) -> float:
        style = ParagraphStyle(
            "body", fontName=BOLD_FONT if bold else BODY_FONT, fontSize=size,
            leading=leading or size * 1.4, textColor=color, alignment=TA_LEFT,
            spaceBefore=0, spaceAfter=0,
        )
        block = Paragraph(text, style)
        _, height = block.wrap(width, PAGE_HEIGHT)
        if top - height < floor:
            raise ValueError(f"Contenido desbordado en página {self.page}: {text[:100]}")
        block.drawOn(self.pdf, x, top - height)
        return top - height

    def label(self, text: str, x: float, baseline: float, size: float = 9, color=MUTED, bold: bool = False) -> None:
        self.pdf.setFillColor(color)
        self.pdf.setFont(BOLD_FONT if bold else BODY_FONT, size)
        self.pdf.drawString(x, baseline, text)

    def new_page(self, section: str, title: str) -> float:
        if self.page:
            self.pdf.showPage()
        self.page += 1
        self.pdf.setFillColor(colors.white)
        self.pdf.rect(0, 0, PAGE_WIDTH, PAGE_HEIGHT, fill=1, stroke=0)
        self.pdf.setFillColor(TEAL)
        self.pdf.rect(0, PAGE_HEIGHT - 8, PAGE_WIDTH, 8, fill=1, stroke=0)
        self.label("RUTAPAQUETE / " + section.upper(), MARGIN, PAGE_HEIGHT - 37, 8, TEAL, True)
        self.pdf.setStrokeColor(LINE)
        self.pdf.line(MARGIN, 45, PAGE_WIDTH - MARGIN, 45)
        self.label("Formulario de registro de su aplicación", MARGIN, 29, 8)
        self.pdf.setFont(BODY_FONT, 8)
        self.pdf.drawRightString(PAGE_WIDTH - MARGIN, 29, f"{self.result['date']}  |  {self.page:02d} / {TOTAL_PAGES:02d}")
        return self.paragraph(escape(title), MARGIN, PAGE_HEIGHT - 61, size=25, leading=30, color=TEAL, bold=True) - 17

    def screenshot(self, name: str, x: float, top: float, width: float, max_height: float) -> tuple[float, float]:
        image = self.images[name]
        image_width, image_height = image.getSize()
        scale = min(width / image_width, max_height / image_height)
        draw_width, draw_height = image_width * scale, image_height * scale
        left = x + (width - draw_width) / 2
        self.pdf.setFillColor(PALE)
        self.pdf.setStrokeColor(LINE)
        self.pdf.roundRect(left - 3, top - draw_height - 3, draw_width + 6, draw_height + 6, 4, fill=1, stroke=1)
        self.pdf.drawImage(image, left, top - draw_height, draw_width, draw_height, mask="auto")
        return top - draw_height, draw_width

    def cover(self) -> None:
        top = self.new_page("Entrega universitaria", "RutaPaquete")
        top = self.paragraph("Formulario de registro y acceso local para una aplicación Android de rastreo de envíos.", MARGIN, top, size=16, leading=22) - 19
        top = self.paragraph("Kotlin + XML + View Binding", MARGIN, top, size=12, bold=True, color=TEAL) - 8
        top = self.paragraph("Room 3.0.3 · KSP 2.3.10 · SQLite Framework 2.7.1", MARGIN, top, size=10, color=MUTED) - 24
        top = self.paragraph("Alcance implementado", MARGIN, top, size=14, bold=True) - 10
        paragraphs = (
            "<b>Inicio de sesión:</b> usuario y contraseña, acceso a registro y validación contra la base local.",
            "<b>Registro:</b> usuario único, nombre, apellidos, edad de 18 a 120 años, dirección, teléfono, contraseña y confirmación. Enviar guarda los datos válidos; Cancelar vuelve sin guardar.",
            "<b>Rastreo:</b> mensaje orientativo, código y botón Buscar paquete. La consulta no tiene backend en esta entrega. Se incluye cierre de sesión.",
        )
        for text in paragraphs:
            top = self.paragraph(text, MARGIN, top, size=10.5) - 11
        top -= 9
        top = self.paragraph("Correspondencia con la actividad", MARGIN, top, size=14, bold=True) - 11
        rows = (
            ("Entidad User", "Id autogenerado, datos personales y credenciales derivadas. Página 2."),
            ("DAO y Singleton", "Inserción, consulta, duplicados y única instancia Room. Páginas 3 y 4."),
            ("Interfaz y corrutinas", "Activities Kotlin, TextInputLayout y lifecycleScope. Páginas 5 a 7."),
            ("Validación y evidencias", "Errores de formulario, alta válida y acceso. Páginas 8 a 10."),
        )
        for label, description in rows:
            self.pdf.setStrokeColor(LINE)
            self.pdf.line(MARGIN, top + 3, PAGE_WIDTH - MARGIN, top + 3)
            self.paragraph(escape(label), MARGIN, top - 4, 132, size=9.5, bold=True)
            bottom = self.paragraph(escape(description), MARGIN + 148, top - 4, WIDTH - 148, size=9.5)
            top = bottom - 13
        top -= 11
        self.paragraph("<b>Datos y seguridad.</b> Room es persistente. Se guarda hash PBKDF2 con salt aleatorio, nunca la contraseña en texto plano ni su confirmación. La autenticación es una simulación local educativa, no un sistema de producción.", MARGIN, top, size=9.5, color=MUTED)

    def code_page(self, name: str, title: str, description: str, source_path: str, *, fragment: bool = False) -> None:
        top = self.new_page("Código / evidencia del editor", title)
        top = self.paragraph(description, MARGIN, top, size=10.5) - 19
        bottom, _ = self.screenshot(name, MARGIN, top, WIDTH, 375 if fragment else 480)
        bottom = self.paragraph("Captura real del editor: " + escape(source_path), MARGIN, bottom - 12, size=8, color=MUTED) - 20
        if fragment:
            bottom = self.paragraph("Inserción asíncrona: fragmento exacto del archivo", MARGIN, bottom, size=11, bold=True) - 8
            lines = REGISTER_SOURCE.read_text(encoding="utf-8-sig").splitlines()
            start = next(i for i, line in enumerate(lines) if "lifecycleScope.launch {" in line)
            end = next(i for i in range(start, len(lines)) if lines[i].strip() == "finish()") + 1
            block = [(i + 1, lines[i][8:] if lines[i].startswith("        ") else lines[i]) for i in range(start, end)]
            line_height = 11
            box_height = 20 + len(block) * line_height
            if bottom - box_height < 61:
                raise ValueError("El fragmento lifecycleScope no cabe en la página del registro.")
            self.pdf.setFillColor(PALE)
            self.pdf.roundRect(MARGIN, bottom - box_height, WIDTH, box_height, 5, fill=1, stroke=0)
            self.pdf.setFont(CODE_FONT, 6.9)
            self.pdf.setFillColor(INK)
            for offset, (number, line) in enumerate(block):
                text = f"{number:>3}  {line}"
                if pdfmetrics.stringWidth(text, CODE_FONT, 6.9) > WIDTH - 18:
                    raise ValueError("Una línea del fragmento real supera el ancho del informe.")
                self.pdf.drawString(MARGIN + 9, bottom - 15 - offset * line_height, text)
        else:
            self.paragraph("La imagen corresponde al código incluido en el ZIP. La implementación completa se conserva en el archivo fuente para su revisión y compilación.", MARGIN, bottom, size=9.5, color=MUTED)

    def app_pair(self, title: str, introduction: str, items: tuple[tuple[str, str, str], tuple[str, str, str]]) -> None:
        top = self.new_page("Interfaz / evidencia de emulador", title)
        top = self.paragraph(introduction, MARGIN, top, size=10.5) - 22
        gap = 22
        cell_width = (WIDTH - gap) / 2
        for index, (name, label, caption) in enumerate(items):
            x = MARGIN + index * (cell_width + gap)
            self.paragraph(escape(label), x, top, cell_width, size=11, bold=True, color=TEAL)
            bottom, _ = self.screenshot(name, x, top - 27, cell_width, 450)
            self.paragraph(escape(caption), x, bottom - 13, cell_width, size=9.2)
        self.paragraph("Capturas reales de la aplicación durante las pruebas instrumentadas. Los datos usados son ficticios; los archivos originales de evidencia se incluyen en el ZIP.", MARGIN, 114, size=8.5, color=MUTED)

    def results(self) -> None:
        top = self.new_page("Verificación / reproducción", "Rastreo y resultados")
        top = self.paragraph(escape(self.result["device"]) + " · " + escape(self.result["date"]), MARGIN, top, size=10, color=MUTED) - 18
        bottom, _ = self.screenshot("08-rastreo", MARGIN, top, 196, 345)
        self.paragraph("Acceso al rastreo después de validar las credenciales. Buscar paquete no realiza una consulta real.", MARGIN, bottom - 11, 196, size=8.7)
        x = MARGIN + 218
        right_width = WIDTH - 218
        y = self.paragraph("Resultados registrados", x, top, right_width, size=12, bold=True, color=TEAL) - 9
        metrics = (
            ("Pruebas unitarias", self.result["unit_tests"]),
            ("Pruebas instrumentadas", self.result["instrumented_tests"]),
            ("Errores Lint", self.result["lint_errors"]),
            ("Advertencias Lint", self.result["lint_warnings"]),
        )
        for label, value in metrics:
            y = self.paragraph(f"{escape(label)}: <b>{value}</b>", x, y, right_width, size=10) - 6
        y -= 7
        y = self.paragraph("Fuente: output/pruebas/resumen.json y reportes exportados incluidos en la entrega.", x, y, right_width, size=8.5, color=MUTED) - 14
        if self.result["notes"]:
            y = self.paragraph("Notas verificadas", x, y, right_width, size=10, bold=True) - 7
            for note in self.result["notes"]:
                y = self.paragraph("- " + escape(note), x, y, right_width, size=8.5, floor=335) - 6
        y = 306
        y = self.paragraph("Cómo reproducir", MARGIN, y, size=12, bold=True, color=TEAL) - 7
        y = self.paragraph("1. Extraer el ZIP y abrir la carpeta del proyecto en Android Studio. Sincronizar Gradle e instalar el SDK solicitado por el proyecto.<br/>2. Iniciar un emulador compatible, activar el teclado en pantalla y ejecutar la app. Para repetir la verificación automatizada:", MARGIN, y, size=9) - 8
        for command in (".\\gradlew.bat :app:testDebugUnitTest :app:lintDebug", ".\\gradlew.bat :app:connectedDebugAndroidTest"):
            self.pdf.setFont(CODE_FONT, 8.2)
            self.pdf.setFillColor(INK)
            self.pdf.drawString(MARGIN + 6, y - 10, command)
            y -= 15
        y -= 8
        y = self.paragraph("<b>Límites:</b> compatibilidad declarada desde API 24; el resumen anterior identifica el dispositivo realmente probado. API 24 no queda certificada salvo evidencia expresa en las notas. La rotación durante un guardado en curso no está certificada. No hay API de paquetería, GPS ni autenticación remota.", MARGIN, y, size=8.3) - 11
        self.paragraph("<b>Referencias:</b> Android Developers, <link href=\"https://developer.android.com/training/data-storage/room\" color=\"#175A63\">Guardar datos con Room</link>. Consigna local: <i>Formulario de registro de su aplicación.pdf</i>. Este informe no publica ni envía la actividad a la universidad.", MARGIN, y, size=8.3)

    def build(self) -> None:
        self.cover()
        local = "app/src/main/java/com/example/myapplication/"
        self.code_page("User", "Entidad User", "La tabla usuarios conserva los campos solicitados por la actividad y los necesarios para el acceso local. El id es autogenerado y el nombre de usuario tiene un índice único. La confirmación de contraseña no se almacena.", local + "data/local/User.kt")
        self.code_page("UserDao", "Acceso a datos: UserDao", "El DAO ofrece inserción, búsqueda por usuario, comprobación de duplicados y consulta de todos los registros. Las operaciones suspend permiten utilizarlas desde corrutinas; las consultas emplean parámetros.", local + "data/local/UserDao.kt")
        self.code_page("AppDatabase", "RoomDatabase Singleton", "AppDatabase declara la entidad, la versión 1 del esquema y el DAO. La instancia se crea con applicationContext y AndroidSQLiteDriver. El patrón Singleton evita construir una base distinta en cada pantalla.", local + "data/local/AppDatabase.kt")
        self.code_page("MainActivity", "Inicio de sesión en Kotlin", "MainActivity utiliza View Binding, exige ambos campos y consulta el repositorio. Una coincidencia del hash inicia la sesión en memoria y abre TrackingActivity; un fallo muestra un mensaje genérico. Crear nuevo usuario abre el registro.", local + "MainActivity.kt")
        self.code_page("RegisterActivity", "Validación y guardado", "El formulario muestra errores por campo, exige mayoría de edad, valida teléfono y compara exactamente las contraseñas. lifecycleScope ejecuta el registro; el repositorio calcula el hash fuera del hilo de interfaz y Room persiste el usuario.", local + "RegisterActivity.kt", fragment=True)
        self.app_pair("Inicio y formulario", "Las pantallas separan el acceso de usuarios existentes y la creación de una cuenta nueva.", (
            ("01-inicio", "Inicio de sesión", "Campos de usuario y contraseña, botón de acceso y opción Crear nuevo usuario."),
            ("02a-formulario", "Crear cuenta", "Formulario desplazable. Dirección, teléfono y contraseñas se encuentran al continuar hacia abajo."),
        ))
        self.app_pair("Validaciones del formulario", "No se permite guardar un formulario vacío ni registrar una edad inferior a 18 años.", (
            ("02-campos-obligatorios", "Campos obligatorios", "Los campos inválidos muestran mensajes antes de intentar insertar el registro."),
            ("03-edad-menor", "Edad mínima", "La prueba usa 17 años y verifica el error. Después se emplean 18 años para el registro válido."),
        ))
        self.app_pair("Contraseñas y registro válido", "La confirmación debe coincidir exactamente. Un registro válido se guarda y devuelve al inicio de sesión.", (
            ("04-contrasenas", "Contraseñas diferentes", "La confirmación incorrecta impide completar el alta."),
            ("05-registro-exitoso", "Usuario registrado", "El mensaje confirma el alta y el usuario queda precargado para iniciar sesión."),
        ))
        self.results()
        if self.page != TOTAL_PAGES:
            raise ValueError(f"Número de páginas inesperado: {self.page}")
        self.pdf.save()


def main() -> None:
    result, images = load_inputs()
    register_fonts()
    DESTINATION.parent.mkdir(parents=True, exist_ok=True)
    temporary = DESTINATION.with_name("RutaPaquete-informe.tmp.pdf")
    try:
        Report(temporary, result, images).build()
        temporary.replace(DESTINATION)
    finally:
        if temporary.exists():
            temporary.unlink()
    print(f"PDF generado: {DESTINATION}")
    print(f"Páginas: {TOTAL_PAGES}. Pendiente renderizar e inspeccionar visualmente todas las páginas.")


if __name__ == "__main__":
    main()
