package com.example.demo.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Generates QR codes using ZXing and returns them as Base64-encoded PNG strings,
 * ready for embedding in Thymeleaf templates or PDF exports.
 */
@Service
public class QrCodeService {

    /**
     * Generates a QR code image for the given content.
     *
     * @param content  text / URL to encode (e.g. a verification URL)
     * @param sizePx   side length in pixels (e.g. 200)
     * @return Base64-encoded PNG data URI string  "data:image/png;base64,..."
     */
    public String generateQrCodeBase64(String content, int sizePx) {
        try {
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            hints.put(EncodeHintType.MARGIN, 1);

            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, sizePx, sizePx, hints);

            BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "PNG", baos);

            String base64 = Base64.getEncoder().encodeToString(baos.toByteArray());
            return "data:image/png;base64," + base64;

        } catch (WriterException | IOException e) {
            throw new RuntimeException("Failed to generate QR code for content: " + content, e);
        }
    }

    /**
     * Builds a standard card-verification URL for a profile UUID.
     *
     * @param uuid      the profile's public UUID
     * @param baseUrl   the application base URL, e.g. "https://cards.myorg.com"
     * @return the full verification URL
     */
    public String buildVerificationUrl(String uuid, String baseUrl) {
        return baseUrl + "/verify/" + uuid;
    }
}
