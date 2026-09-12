package game.hall.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import game.hall.exception.HallException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class QrCodeUtil {

    private QrCodeUtil() {
    }

    public static List<String> encodeRows(String content) {
        if (content == null || content.isBlank()) {
            throw new HallException("QR code content is required");
        }

        try {
            QRCodeWriter writer = new QRCodeWriter();
            var matrix = writer.encode(content, BarcodeFormat.QR_CODE, 1, 1, Map.of(
                    EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M,
                    EncodeHintType.MARGIN, 1
            ));

            List<String> rows = new ArrayList<>(matrix.getHeight());
            for (int y = 0; y < matrix.getHeight(); y++) {
                StringBuilder row = new StringBuilder(matrix.getWidth());
                for (int x = 0; x < matrix.getWidth(); x++) {
                    row.append(matrix.get(x, y) ? '1' : '0');
                }
                rows.add(row.toString());
            }
            return rows;
        } catch (Exception exception) {
            throw new HallException("Unable to generate QR code");
        }
    }
}
