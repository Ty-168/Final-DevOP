package com.example.demo.service;

import com.example.demo.model.BarcodeType;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;

/**
 * Generates linear barcodes (Code-128, EAN-13) using ZXing.
 * Returns Base64-encoded PNG strings for HTML/PDF embedding.
 */
@Service
public class BarcodeService {

    private static final int DEFAULT_WIDTH  = 300;
    private static final int DEFAULT_HEIGHT = 80;

    /**
     * Generates a barcode image and returns it as a Base64 data URI.
     *
     * @param content     the text/number to encode
     * @param barcodeType CODE_128 or EAN_13
     * @return "data:image/png;base64,..." string
     */
    public String generateBarcodeBase64(String content, BarcodeType barcodeType) {
        BarcodeFormat format = resolveFormat(barcodeType);

        // EAN-13 requires exactly 12 digit characters (13th is check digit, added by ZXing)
        String encoded = barcodeType == BarcodeType.EAN_13
                ? padEan13(content)
                : content;

        try {
            MultiFormatWriter writer = new MultiFormatWriter();
            BitMatrix matrix = writer.encode(
                    encoded,
                    format,
                    DEFAULT_WIDTH,
                    DEFAULT_HEIGHT,
                    Map.of(EncodeHintType.MARGIN, 5)
            );

            BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "PNG", baos);

            String base64 = Base64.getEncoder().encodeToString(baos.toByteArray());
            return "data:image/png;base64," + base64;

        } catch (WriterException | IOException e) {
            throw new RuntimeException(
                    "Failed to generate " + barcodeType + " barcode for: " + content, e);
        }
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private BarcodeFormat resolveFormat(BarcodeType type) {
        return switch (type) {
            case EAN_13  -> BarcodeFormat.EAN_13;
            default      -> BarcodeFormat.CODE_128;
        };
    }

    /**
     * EAN-13 requires exactly 12 numeric digits as input (ZXing adds check digit).
     * Pads/truncates the registration number to 12 digits.
     */
    private String padEan13(String content) {
        // Strip non-digits
        String digits = content.replaceAll("\\D", "");
        if (digits.length() > 12) digits = digits.substring(0, 12);
        // Left-pad with zeros to reach 12 chars
        return String.format("%12s", digits).replace(' ', '0');
    }
}
