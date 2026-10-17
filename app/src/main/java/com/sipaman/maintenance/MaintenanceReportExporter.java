package com.sipaman.maintenance;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.util.Calendar;

/** Writes report files only; Firebase and UI state remain owned by the Activity. */
public final class MaintenanceReportExporter {

    private static final int PDF_PAGE_WIDTH = 842;
    private static final int PDF_PAGE_HEIGHT = 595;
    private static final float PDF_MARGIN = 30f;
    private static final float PDF_BOTTOM = PDF_PAGE_HEIGHT - PDF_MARGIN;
    private static final float PDF_CELL_PADDING = 4f;
    private static final float[] PDF_COLUMN_WIDTHS = {30f, 260f, 135f, 105f, 145f, 100f};
    private static final String[] PDF_HEADERS = {
            "No", "Task / Deskripsi", "Project / Jenis", "PIC / Prioritas", "Mulai / Due", "Status"
    };

    private MaintenanceReportExporter() {
    }

    /** Immutable report counts shared by both output formats. */
    public static final class Summary {
        private final int totalTask;
        private final int doneTask;
        private final int progressTask;
        private final int pendingTask;
        private final int overdueTask;
        private final int totalProject;

        public Summary(
                int totalTask,
                int doneTask,
                int progressTask,
                int pendingTask,
                int overdueTask,
                int totalProject) {
            this.totalTask = totalTask;
            this.doneTask = doneTask;
            this.progressTask = progressTask;
            this.pendingTask = pendingTask;
            this.overdueTask = overdueTask;
            this.totalProject = totalProject;
        }

        public int getTotalTask() {
            return totalTask;
        }

        public int getDoneTask() {
            return doneTask;
        }

        public int getProgressTask() {
            return progressTask;
        }

        public int getPendingTask() {
            return pendingTask;
        }

        public int getOverdueTask() {
            return overdueTask;
        }

        public int getTotalProject() {
            return totalProject;
        }

        public int getCompletionRate() {
            return totalTask == 0 ? 0 : Math.round((doneTask * 100f) / totalTask);
        }
    }

    /** Creates a paginated landscape PDF and repeats its table header on each page. */
    public static void writePdf(
            OutputStream outputStream,
            List<Task> tasks,
            Summary summary,
            String generatedAt,
            String filterDescription) throws IOException {

        PdfDocument document = new PdfDocument();
        PdfPageState pageState = null;

        try {
            pageState = startPdfPage(document, 1, true, summary, generatedAt, filterDescription);
            int taskNumber = 1;

            for (Task task : tasks) {
                if (task == null) continue;
                pageState = drawTaskRow(document, pageState, taskNumber++, task, summary,
                        generatedAt, filterDescription);
            }

            if (tasks.isEmpty()) {
                Paint emptyPaint = bodyPaint(11f, Color.rgb(100, 116, 139));
                pageState.canvas.drawText("Tidak ada task yang sesuai dengan filter laporan.",
                        PDF_MARGIN, pageState.y + 18f, emptyPaint);
            }

            document.finishPage(pageState.page);
            document.writeTo(outputStream);
        } finally {
            document.close();
        }
    }

    /** Creates a real Office Open XML workbook; it is not a CSV file with a renamed extension. */
    public static void writeExcel(
            OutputStream outputStream,
            List<Task> tasks,
            Summary summary,
            String generatedAt,
            String filterDescription) throws IOException {

        ZipOutputStream zip = new ZipOutputStream(outputStream);
        writeZipEntry(zip, "[Content_Types].xml", contentTypesXml());
        writeZipEntry(zip, "_rels/.rels", rootRelationshipsXml());
        writeZipEntry(zip, "docProps/app.xml", appPropertiesXml());
        writeZipEntry(zip, "docProps/core.xml", corePropertiesXml());
        writeZipEntry(zip, "xl/workbook.xml", workbookXml());
        writeZipEntry(zip, "xl/_rels/workbook.xml.rels", workbookRelationshipsXml());
        writeZipEntry(zip, "xl/styles.xml", stylesXml());
        writeZipEntry(zip, "xl/worksheets/sheet1.xml",
                worksheetXml(tasks, summary, generatedAt, filterDescription));
        zip.finish();
        zip.flush();
    }

    private static PdfPageState startPdfPage(
            PdfDocument document,
            int pageNumber,
            boolean firstPage,
            Summary summary,
            String generatedAt,
            String filterDescription) {

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(
                PDF_PAGE_WIDTH,
                PDF_PAGE_HEIGHT,
                pageNumber
        ).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        float y;

        if (firstPage) {
            y = drawPdfReportHeader(canvas, summary, generatedAt, filterDescription);
        } else {
            y = drawPdfContinuationHeader(canvas, pageNumber, generatedAt, filterDescription);
        }

        y = drawPdfTableHeader(canvas, y + 12f);
        return new PdfPageState(page, canvas, y, pageNumber);
    }

    private static float drawPdfReportHeader(
            Canvas canvas,
            Summary summary,
            String generatedAt,
            String filterDescription) {

        Paint bannerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bannerPaint.setColor(Color.rgb(46, 125, 50));
        canvas.drawRoundRect(PDF_MARGIN, PDF_MARGIN, PDF_PAGE_WIDTH - PDF_MARGIN,
                PDF_MARGIN + 48f, 8f, 8f, bannerPaint);

        Paint titlePaint = bodyPaint(18f, Color.WHITE);
        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("LAPORAN MAINTENANCE SI-PAMAN", PDF_MARGIN + 16f,
                PDF_MARGIN + 30f, titlePaint);

        Paint subTitlePaint = bodyPaint(9f, Color.WHITE);
        canvas.drawText("Ringkasan dan detail task", PDF_MARGIN + 16f,
                PDF_MARGIN + 42f, subTitlePaint);

        Paint labelPaint = bodyPaint(9f, Color.rgb(71, 85, 105));
        float y = PDF_MARGIN + 67f;
        canvas.drawText("Tanggal pembuatan: " + safe(generatedAt), PDF_MARGIN, y, labelPaint);
        y += 13f;
        y = drawWrappedText(canvas, "Filter: " + safe(filterDescription), PDF_MARGIN, y,
                PDF_PAGE_WIDTH - (PDF_MARGIN * 2), labelPaint, 11f) + 4f;

        Paint summaryPaint = bodyPaint(9f, Color.rgb(30, 41, 59));
        summaryPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        Paint summaryBackground = new Paint(Paint.ANTI_ALIAS_FLAG);
        summaryBackground.setColor(Color.rgb(232, 245, 233));
        canvas.drawRoundRect(PDF_MARGIN, y, PDF_PAGE_WIDTH - PDF_MARGIN, y + 38f,
                6f, 6f, summaryBackground);

        float summaryY = y + 16f;
        canvas.drawText("Total: " + summary.getTotalTask(), PDF_MARGIN + 10f, summaryY, summaryPaint);
        canvas.drawText("Selesai: " + summary.getDoneTask(), PDF_MARGIN + 128f, summaryY, summaryPaint);
        canvas.drawText("On Progress: " + summary.getProgressTask(), PDF_MARGIN + 246f,
                summaryY, summaryPaint);
        canvas.drawText("Pending: " + summary.getPendingTask(), PDF_MARGIN + 394f,
                summaryY, summaryPaint);
        canvas.drawText("Overdue: " + summary.getOverdueTask(), PDF_MARGIN + 506f,
                summaryY, summaryPaint);
        canvas.drawText("Completion: " + summary.getCompletionRate() + "%", PDF_MARGIN + 620f,
                summaryY, summaryPaint);
        canvas.drawText("Project: " + summary.getTotalProject(), PDF_MARGIN + 10f,
                summaryY + 14f, bodyPaint(8.5f, Color.rgb(71, 85, 105)));
        return y + 42f;
    }

    private static float drawPdfContinuationHeader(
            Canvas canvas,
            int pageNumber,
            String generatedAt,
            String filterDescription) {

        Paint titlePaint = bodyPaint(13f, Color.rgb(30, 41, 59));
        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("LAPORAN MAINTENANCE SI-PAMAN", PDF_MARGIN, PDF_MARGIN + 14f, titlePaint);

        Paint subTitlePaint = bodyPaint(8.5f, Color.rgb(100, 116, 139));
        canvas.drawText("Halaman " + pageNumber + " | " + safe(generatedAt), PDF_MARGIN,
                PDF_MARGIN + 28f, subTitlePaint);
        canvas.drawText("Filter: " + safe(filterDescription), PDF_MARGIN, PDF_MARGIN + 40f,
                subTitlePaint);
        return PDF_MARGIN + 44f;
    }

    private static float drawPdfTableHeader(Canvas canvas, float y) {
        Paint background = new Paint(Paint.ANTI_ALIAS_FLAG);
        background.setColor(Color.rgb(22, 101, 52));
        Paint textPaint = bodyPaint(8f, Color.WHITE);
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        float x = PDF_MARGIN;
        float height = 24f;

        for (int index = 0; index < PDF_HEADERS.length; index++) {
            canvas.drawRect(x, y, x + PDF_COLUMN_WIDTHS[index], y + height, background);
            drawCenteredText(canvas, PDF_HEADERS[index], x, y, PDF_COLUMN_WIDTHS[index], height, textPaint);
            x += PDF_COLUMN_WIDTHS[index];
        }
        return y + height;
    }

    private static PdfPageState drawTaskRow(
            PdfDocument document,
            PdfPageState state,
            int taskNumber,
            Task task,
            Summary summary,
            String generatedAt,
            String filterDescription) {

        Paint textPaint = bodyPaint(8.5f, Color.rgb(30, 41, 59));
        float lineHeight = 11f;
        List<List<String>> cells = createPdfCells(taskNumber, task, textPaint);
        int maxLineCount = 1;
        for (List<String> cell : cells) {
            maxLineCount = Math.max(maxLineCount, cell.size());
        }

        int lineStart = 0;
        while (lineStart < maxLineCount) {
            int availableLines = (int) Math.floor(
                    (PDF_BOTTOM - state.y - (PDF_CELL_PADDING * 2f)) / lineHeight
            );
            if (availableLines < 1) {
                document.finishPage(state.page);
                state = startPdfPage(document, state.pageNumber + 1, false, summary,
                        generatedAt, filterDescription);
                availableLines = (int) Math.floor(
                        (PDF_BOTTOM - state.y - (PDF_CELL_PADDING * 2f)) / lineHeight
                );
            }

            int linesForPage = Math.min(maxLineCount - lineStart, Math.max(1, availableLines));
            float rowHeight = (linesForPage * lineHeight) + (PDF_CELL_PADDING * 2f);
            drawPdfRowChunk(state.canvas, state.y, rowHeight, cells, lineStart, linesForPage,
                    lineHeight, textPaint);
            state.y += rowHeight;
            lineStart += linesForPage;
        }
        return state;
    }

    private static List<List<String>> createPdfCells(int taskNumber, Task task, Paint paint) {
        List<List<String>> cells = new ArrayList<>();
        cells.add(wrapText(String.valueOf(taskNumber), PDF_COLUMN_WIDTHS[0] - (PDF_CELL_PADDING * 2f), paint));
        cells.add(wrapText("Judul: " + display(task.getJudul()) + "\nDeskripsi: "
                + display(task.getDeskripsi()), PDF_COLUMN_WIDTHS[1] - (PDF_CELL_PADDING * 2f), paint));
        cells.add(wrapText("Project: " + displayProject(task) + "\nJenis: "
                + display(task.getJenis()), PDF_COLUMN_WIDTHS[2] - (PDF_CELL_PADDING * 2f), paint));
        cells.add(wrapText("PIC: " + display(task.getPic()) + "\nPrioritas: "
                + display(task.getPriority()), PDF_COLUMN_WIDTHS[3] - (PDF_CELL_PADDING * 2f), paint));
        cells.add(wrapText(
                "Mulai: " + display(task.getMulai())
                        + "\nDue: " + display(task.getDue())
                        + "\nSelesai: " + getTanggalSelesai(task)
                        + "\nDurasi: " + getDurasiPengerjaan(task)
                        + "\nKetepatan: " + getKetepatanWaktu(task),
                PDF_COLUMN_WIDTHS[4] - (PDF_CELL_PADDING * 2f),
                paint
        ));
        cells.add(wrapText(getDisplayStatus(task), PDF_COLUMN_WIDTHS[5] - (PDF_CELL_PADDING * 2f), paint));
        return cells;
    }

    private static void drawPdfRowChunk(
            Canvas canvas,
            float y,
            float rowHeight,
            List<List<String>> cells,
            int lineStart,
            int lineCount,
            float lineHeight,
            Paint textPaint) {

        Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(0.6f);
        borderPaint.setColor(Color.rgb(203, 213, 225));
        float x = PDF_MARGIN;
        Paint.FontMetrics metrics = textPaint.getFontMetrics();
        float firstBaseline = y + PDF_CELL_PADDING - metrics.ascent;

        for (int column = 0; column < cells.size(); column++) {
            float width = PDF_COLUMN_WIDTHS[column];
            canvas.drawRect(x, y, x + width, y + rowHeight, borderPaint);
            List<String> lines = cells.get(column);
            for (int line = 0; line < lineCount; line++) {
                int lineIndex = lineStart + line;
                if (lineIndex < lines.size()) {
                    canvas.drawText(lines.get(lineIndex), x + PDF_CELL_PADDING,
                            firstBaseline + (line * lineHeight), textPaint);
                }
            }
            x += width;
        }
    }

    private static List<String> wrapText(String value, float maxWidth, Paint paint) {
        List<String> result = new ArrayList<>();
        String[] paragraphs = safe(value).split("\\n", -1);
        for (String paragraph : paragraphs) {
            String trimmed = paragraph.trim();
            if (trimmed.isEmpty()) {
                result.add("");
                continue;
            }

            String[] words = trimmed.split("\\s+");
            String currentLine = "";
            for (String word : words) {
                String candidate = currentLine.isEmpty() ? word : currentLine + " " + word;
                if (paint.measureText(candidate) <= maxWidth) {
                    currentLine = candidate;
                } else if (currentLine.isEmpty()) {
                    appendLongWord(result, word, maxWidth, paint);
                } else {
                    result.add(currentLine);
                    if (paint.measureText(word) <= maxWidth) {
                        currentLine = word;
                    } else {
                        appendLongWord(result, word, maxWidth, paint);
                        currentLine = "";
                    }
                }
            }
            if (!currentLine.isEmpty()) {
                result.add(currentLine);
            }
        }
        if (result.isEmpty()) result.add("-");
        return result;
    }

    private static void appendLongWord(List<String> lines, String word, float maxWidth, Paint paint) {
        StringBuilder part = new StringBuilder();
        for (int index = 0; index < word.length(); index++) {
            part.append(word.charAt(index));
            if (paint.measureText(part.toString()) > maxWidth && part.length() > 1) {
                char overflow = part.charAt(part.length() - 1);
                part.deleteCharAt(part.length() - 1);
                lines.add(part.toString());
                part.setLength(0);
                part.append(overflow);
            }
        }
        if (part.length() > 0) lines.add(part.toString());
    }

    private static float drawWrappedText(
            Canvas canvas,
            String value,
            float x,
            float y,
            float maxWidth,
            Paint paint,
            float lineHeight) {

        List<String> lines = wrapText(value, maxWidth, paint);
        for (String line : lines) {
            canvas.drawText(line, x, y, paint);
            y += lineHeight;
        }
        return y;
    }

    private static void drawCenteredText(
            Canvas canvas,
            String value,
            float x,
            float y,
            float width,
            float height,
            Paint paint) {
        Paint.FontMetrics metrics = paint.getFontMetrics();
        float baseline = y + ((height - metrics.bottom + metrics.top) / 2f) - metrics.top;
        canvas.drawText(value, x + ((width - paint.measureText(value)) / 2f), baseline, paint);
    }

    private static Paint bodyPaint(float textSize, int color) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextSize(textSize);
        paint.setColor(color);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        return paint;
    }

    private static String worksheetXml(
            List<Task> tasks,
            Summary summary,
            String generatedAt,
            String filterDescription) {

        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
        xml.append("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">");
        xml.append("<sheetViews><sheetView workbookViewId=\"0\"><pane ySplit=\"10\" topLeftCell=\"A11\" ")
                .append("activePane=\"bottomLeft\" state=\"frozen\"/><selection pane=\"bottomLeft\"/>")
                .append("</sheetView></sheetViews>");
        xml.append("<sheetFormatPr defaultRowHeight=\"15\"/>");
        xml.append("<cols>")
                .append(columnXml(1, 1, 7))    // No
                .append(columnXml(2, 2, 28))   // Judul
                .append(columnXml(3, 3, 22))   // Project
                .append(columnXml(4, 4, 20))   // Jenis
                .append(columnXml(5, 5, 16))   // PIC
                .append(columnXml(6, 6, 12))   // Prioritas
                .append(columnXml(7, 8, 15))   // Mulai dan Due
                .append(columnXml(9, 9, 17))   // Tanggal selesai
                .append(columnXml(10, 10, 15)) // Durasi
                .append(columnXml(11, 11, 22)) // Ketepatan waktu
                .append(columnXml(12, 12, 17)) // Status
                .append(columnXml(13, 13, 40)) // Deskripsi
                .append("</cols><sheetData>");

        appendRow(xml, 1, stringCell("A1", "LAPORAN MAINTENANCE SI-PAMAN", 2));
        appendRow(xml, 2, stringCell("A2", "Tanggal pembuatan: " + safe(generatedAt), 0));
        appendRow(xml, 3, stringCell("A3", "Filter: " + safe(filterDescription), 0));
        appendRow(xml, 5, stringCell("A5", "RINGKASAN", 4));
        appendRow(xml, 6,
                stringCell("A6", "Total Task", 1),
                stringCell("B6", "Selesai", 1),
                stringCell("C6", "On Progress", 1),
                stringCell("D6", "Pending", 1),
                stringCell("E6", "Overdue", 1),
                stringCell("F6", "Completion Rate", 1),
                stringCell("G6", "Project", 1));
        appendRow(xml, 7,
                numberCell("A7", summary.getTotalTask(), 3),
                numberCell("B7", summary.getDoneTask(), 3),
                numberCell("C7", summary.getProgressTask(), 3),
                numberCell("D7", summary.getPendingTask(), 3),
                numberCell("E7", summary.getOverdueTask(), 3),
                stringCell("F7", summary.getCompletionRate() + "%", 3),
                numberCell("G7", summary.getTotalProject(), 3));
        appendRow(xml, 9, stringCell("A9", "DETAIL TASK", 4));
        appendRow(xml, 10,
                stringCell("A10", "No", 1),
                stringCell("B10", "Judul Task", 1),
                stringCell("C10", "Project", 1),
                stringCell("D10", "Jenis Maintenance", 1),
                stringCell("E10", "PIC", 1),
                stringCell("F10", "Prioritas", 1),
                stringCell("G10", "Tanggal Mulai", 1),
                stringCell("H10", "Due Date", 1),
                stringCell("I10", "Tanggal Selesai", 1),
                stringCell("J10", "Durasi (Hari)", 1),
                stringCell("K10", "Ketepatan Waktu", 1),
                stringCell("L10", "Status", 1),
                stringCell("M10", "Deskripsi", 1));

        int row = 11;
        int number = 1;
        for (Task task : tasks) {
            if (task == null) continue;
            int currentRow = row++;

            appendRow(xml, currentRow,
                    numberCell("A" + currentRow, number++, 3),
                    stringCell("B" + currentRow, display(task.getJudul()), 3),
                    stringCell("C" + currentRow, displayProject(task), 3),
                    stringCell("D" + currentRow, display(task.getJenis()), 3),
                    stringCell("E" + currentRow, display(task.getPic()), 3),
                    stringCell("F" + currentRow, display(task.getPriority()), 3),
                    stringCell("G" + currentRow, display(task.getMulai()), 3),
                    stringCell("H" + currentRow, display(task.getDue()), 3),
                    stringCell("I" + currentRow, getTanggalSelesai(task), 3),
                    stringCell("J" + currentRow, getDurasiPengerjaan(task), 3),
                    stringCell("K" + currentRow, getKetepatanWaktu(task), 3),
                    stringCell("L" + currentRow, getDisplayStatus(task), 3),
                    stringCell("M" + currentRow, display(task.getDeskripsi()), 3));
        }

        xml.append("</sheetData>");

        // AutoFilter harus ditulis sebelum mergeCells pada worksheet XML.
        if (row > 11) {
            xml.append("<autoFilter ref=\"A10:M")
                    .append(row - 1)
                    .append("\"/>");
        }

        xml.append("<mergeCells count=\"5\">")
                .append("<mergeCell ref=\"A1:M1\"/>")
                .append("<mergeCell ref=\"A2:M2\"/>")
                .append("<mergeCell ref=\"A3:M3\"/>")
                .append("<mergeCell ref=\"A5:M5\"/>")
                .append("<mergeCell ref=\"A9:M9\"/>")
                .append("</mergeCells>");

        xml.append("</worksheet>");
        return xml.toString();
    }

    private static String columnXml(int min, int max, int width) {
        return "<col min=\"" + min + "\" max=\"" + max + "\" width=\"" + width
                + "\" customWidth=\"1\"/>";
    }

    private static void appendRow(StringBuilder xml, int row, String... cells) {
        xml.append("<row r=\"").append(row).append("\">");
        for (String cell : cells) {
            xml.append(cell);
        }
        xml.append("</row>");
    }

    private static String stringCell(String reference, String value, int style) {
        return "<c r=\"" + reference + "\" s=\"" + style
                + "\" t=\"inlineStr\"><is><t xml:space=\"preserve\">"
                + xmlEscape(safe(value)) + "</t></is></c>";
    }

    private static String numberCell(String reference, int value, int style) {
        return "<c r=\"" + reference + "\" s=\"" + style + "\"><v>" + value + "</v></c>";
    }

    private static void writeZipEntry(ZipOutputStream zip, String path, String content)
            throws IOException {
        zip.putNextEntry(new ZipEntry(path));
        zip.write(content.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }

    private static String contentTypesXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">"
                + "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>"
                + "<Default Extension=\"xml\" ContentType=\"application/xml\"/>"
                + "<Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>"
                + "<Override PartName=\"/xl/worksheets/sheet1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>"
                + "<Override PartName=\"/xl/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml\"/>"
                + "<Override PartName=\"/docProps/core.xml\" ContentType=\"application/vnd.openxmlformats-package.core-properties+xml\"/>"
                + "<Override PartName=\"/docProps/app.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.extended-properties+xml\"/>"
                + "</Types>";
    }

    private static String rootRelationshipsXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"xl/workbook.xml\"/>"
                + "<Relationship Id=\"rId2\" Type=\"http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties\" Target=\"docProps/core.xml\"/>"
                + "<Relationship Id=\"rId3\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/extended-properties\" Target=\"docProps/app.xml\"/>"
                + "</Relationships>";
    }

    private static String appPropertiesXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<Properties xmlns=\"http://schemas.openxmlformats.org/officeDocument/2006/extended-properties\" "
                + "xmlns:vt=\"http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes\">"
                + "<Application>SI-PAMAN</Application></Properties>";
    }

    private static String corePropertiesXml() {
        String now = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(new Date());
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<cp:coreProperties xmlns:cp=\"http://schemas.openxmlformats.org/package/2006/metadata/core-properties\" "
                + "xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:dcterms=\"http://purl.org/dc/terms/\" "
                + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
                + "<dc:creator>SI-PAMAN</dc:creator><cp:lastModifiedBy>SI-PAMAN</cp:lastModifiedBy>"
                + "<dcterms:created xsi:type=\"dcterms:W3CDTF\">" + now + "</dcterms:created>"
                + "<dcterms:modified xsi:type=\"dcterms:W3CDTF\">" + now + "</dcterms:modified>"
                + "</cp:coreProperties>";
    }

    private static String workbookXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" "
                + "xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">"
                + "<sheets><sheet name=\"Laporan Maintenance\" sheetId=\"1\" r:id=\"rId1\"/>"
                + "</sheets></workbook>";
    }

    private static String workbookRelationshipsXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet1.xml\"/>"
                + "<Relationship Id=\"rId2\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles\" Target=\"styles.xml\"/>"
                + "</Relationships>";
    }

    private static String stylesXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<styleSheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">"
                + "<fonts count=\"3\"><font><sz val=\"10\"/><name val=\"Arial\"/></font>"
                + "<font><b/><color rgb=\"FFFFFFFF\"/><sz val=\"10\"/><name val=\"Arial\"/></font>"
                + "<font><b/><color rgb=\"FF1F2937\"/><sz val=\"16\"/><name val=\"Arial\"/></font></fonts>"
                + "<fills count=\"3\"><fill><patternFill patternType=\"none\"/></fill>"
                + "<fill><patternFill patternType=\"gray125\"/></fill>"
                + "<fill><patternFill patternType=\"solid\"><fgColor rgb=\"FF2E7D32\"/><bgColor indexed=\"64\"/>"
                + "</patternFill></fill></fills>"
                + "<borders count=\"2\"><border><left/><right/><top/><bottom/><diagonal/></border>"
                + "<border><left style=\"thin\"><color rgb=\"FFCBD5E1\"/></left><right style=\"thin\"><color rgb=\"FFCBD5E1\"/></right>"
                + "<top style=\"thin\"><color rgb=\"FFCBD5E1\"/></top><bottom style=\"thin\"><color rgb=\"FFCBD5E1\"/></bottom><diagonal/></border></borders>"
                + "<cellStyleXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\"/></cellStyleXfs>"
                + "<cellXfs count=\"5\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\"/>"
                + "<xf numFmtId=\"0\" fontId=\"1\" fillId=\"2\" borderId=\"1\" applyFont=\"1\" applyFill=\"1\" applyBorder=\"1\" xfId=\"0\"/>"
                + "<xf numFmtId=\"0\" fontId=\"2\" fillId=\"0\" borderId=\"0\" applyFont=\"1\" xfId=\"0\"/>"
                + "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"1\" applyBorder=\"1\" xfId=\"0\"/>"
                + "<xf numFmtId=\"0\" fontId=\"1\" fillId=\"2\" borderId=\"0\" applyFont=\"1\" applyFill=\"1\" xfId=\"0\"/></cellXfs>"
                + "<cellStyles count=\"1\"><cellStyle name=\"Normal\" xfId=\"0\" builtinId=\"0\"/></cellStyles>"
                + "</styleSheet>";
    }

    private static String getDisplayStatus(Task task) {
        String status = normalizeStatus(task.getStatus());
        if ("DONE".equals(status)) return "Selesai";
        if (isOverdue(task)) return "Overdue";
        if ("ON_PROGRESS".equals(status)) return "On Progress";
        return "Pending";
    }


    private static Date parseDate(String value) {
        if (isEmpty(value)) return null;

        try {
            SimpleDateFormat format =
                    new SimpleDateFormat("d/M/yyyy", Locale.US);
            format.setLenient(false);
            return format.parse(value.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private static String getTanggalSelesai(Task task) {
        Date selesai = parseDate(task.getTanggalSelesai());
        if (selesai == null) return "-";

        return new SimpleDateFormat(
                "dd/MM/yyyy", Locale.US
        ).format(selesai);
    }

    private static String getDurasiPengerjaan(Task task) {
        Date mulai = parseDate(task.getMulai());
        Date selesai = parseDate(task.getTanggalSelesai());

        if (mulai == null || selesai == null) return "-";

        long selisih = selesai.getTime() - mulai.getTime();
        long hari = Math.round(selisih / 86400000.0);

        if (hari < 0) return "Tanggal tidak valid";

        return hari + " hari";
    }

    private static String getKetepatanWaktu(Task task) {
        String status = normalizeStatus(task.getStatus());

        if (!"DONE".equals(status)) {
            return isOverdue(task) ? "Overdue" : "Belum Selesai";
        }

        Date selesai = parseDate(task.getTanggalSelesai());
        Date due = parseDate(task.getDue());

        if (selesai == null || due == null) {
            return "Data belum tersedia";
        }

        Calendar selesaiCal = Calendar.getInstance();
        selesaiCal.setTime(selesai);
        selesaiCal.set(Calendar.HOUR_OF_DAY, 0);
        selesaiCal.set(Calendar.MINUTE, 0);
        selesaiCal.set(Calendar.SECOND, 0);
        selesaiCal.set(Calendar.MILLISECOND, 0);

        Calendar dueCal = Calendar.getInstance();
        dueCal.setTime(due);
        dueCal.set(Calendar.HOUR_OF_DAY, 0);
        dueCal.set(Calendar.MINUTE, 0);
        dueCal.set(Calendar.SECOND, 0);
        dueCal.set(Calendar.MILLISECOND, 0);

        return selesaiCal.getTimeInMillis() <= dueCal.getTimeInMillis()
                ? "Tepat Waktu"
                : "Terlambat";
    }

    private static boolean isOverdue(Task task) {
        if (task == null
                || "DONE".equals(normalizeStatus(task.getStatus()))
                || isEmpty(task.getDue())) {
            return false;
        }

        try {
            Date dueDate = new SimpleDateFormat(
                    "d/M/yyyy", Locale.getDefault()
            ).parse(task.getDue());

            if (dueDate == null) return false;

            Calendar due = Calendar.getInstance();
            due.setTime(dueDate);
            due.set(Calendar.HOUR_OF_DAY, 0);
            due.set(Calendar.MINUTE, 0);
            due.set(Calendar.SECOND, 0);
            due.set(Calendar.MILLISECOND, 0);

            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);

            return due.before(today);

        } catch (Exception ignored) {
            return false;
        }
    }

    private static String normalizeStatus(String status) {
        if (status == null) return "";
        String normalized = status.trim().toUpperCase(Locale.US).replace(" ", "_");
        if (normalized.contains("DONE")) return "DONE";
        if (normalized.contains("PROGRESS")) return "ON_PROGRESS";
        if (normalized.contains("OVERDUE")) return "OVERDUE";
        return normalized;
    }

    private static String displayProject(Task task) {
        if (!isEmpty(task.getProject())) return task.getProject().trim();
        return display(task.getProjectId());
    }

    private static String display(String value) {
        return isEmpty(value) ? "-" : value.trim();
    }

    private static String safe(String value) {
        return value == null ? "-" : value;
    }

    private static boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static String xmlEscape(String value) {
        StringBuilder escaped = new StringBuilder();
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            switch (character) {
                case '&':
                    escaped.append("&amp;");
                    break;
                case '<':
                    escaped.append("&lt;");
                    break;
                case '>':
                    escaped.append("&gt;");
                    break;
                case '"':
                    escaped.append("&quot;");
                    break;
                case '\'':
                    escaped.append("&apos;");
                    break;
                default:
                    if (character >= 0x20 || character == '\n' || character == '\r' || character == '\t') {
                        escaped.append(character);
                    }
                    break;
            }
        }
        return escaped.toString();
    }

    private static final class PdfPageState {
        private final PdfDocument.Page page;
        private final Canvas canvas;
        private float y;
        private final int pageNumber;

        private PdfPageState(PdfDocument.Page page, Canvas canvas, float y, int pageNumber) {
            this.page = page;
            this.canvas = canvas;
            this.y = y;
            this.pageNumber = pageNumber;
        }
    }
}
