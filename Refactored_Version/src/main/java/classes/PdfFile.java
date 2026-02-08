package classes;

import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.properties.VerticalAlignment;

public abstract class PdfFile{

    public void createHeader(Document document, String logoPath, String title, int size) throws Exception {
        Image logo = new Image(ImageDataFactory.create(logoPath))
                .setWidth(50)
                .setHeight(50);

        //create three columns: left logo, title, right empty cell
        float[] widths = {80, 400, 80};
        Table table = new Table(widths)
                .setWidth(UnitValue.createPercentValue(100));

        //create left logo
        table.addCell(
                new Cell()
                        .add(logo)
                        .setBorder(Border.NO_BORDER)
                        .setVerticalAlignment(VerticalAlignment.MIDDLE)
        );

        //create title
        table.addCell(
                new Cell()
                        .add(new Paragraph(title)
                                .setTextAlignment(TextAlignment.CENTER)
                                .setFontSize(size)
                                .simulateBold()
                        )
                        .setBorder(Border.NO_BORDER)
                        .setVerticalAlignment(VerticalAlignment.MIDDLE)
        );

        //create empty cell on the side
        table.addCell(
                new Cell()
                        .setBorder(Border.NO_BORDER)
        );

        document.add(table);
        document.add(new Paragraph("\n\n"));
    }

    public abstract void generateReport(Student student, boolean introduction, Document document) throws Exception;
}