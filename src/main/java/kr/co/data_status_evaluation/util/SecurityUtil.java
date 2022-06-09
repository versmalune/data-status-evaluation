package kr.co.data_status_evaluation.util;

import java.io.UnsupportedEncodingException;

public class SecurityUtil {

    public static String encryptBySeed(String strUserKey, String strPlainTxt) {
        int aiRoundKey[] = new int[32];
        byte abUserKey[] = (byte[]) null;
        byte abPlainTxt[] = (byte[]) null;
        byte abPlainTxt16[] = new byte[16];
        byte abCipherTxt[] = (byte[]) null;
        byte abCipherTxt16[] = new byte[16];
        if (strUserKey == null || "".equals(strUserKey.trim())
                || "null".equals(strUserKey))
            abUserKey = "eastdg-SamlSsoSK".getBytes();
        else
            abUserKey = strUserKey.getBytes();
        int lenPlainTxt = strPlainTxt.getBytes().length;
        if (lenPlainTxt % 16 == 0) {
            abPlainTxt = strPlainTxt.getBytes();
        } else {
            int size = strPlainTxt.getBytes().length;

            if (size < 0 || size > 25) {
                throw new IllegalArgumentException("out of bound");
            }

            lenPlainTxt = ( size/ 16 + 1) * 16;
            abPlainTxt = new byte[lenPlainTxt];
            for (int i = size; i < lenPlainTxt; i++)
                abPlainTxt[i] = 0;

            System.arraycopy(strPlainTxt.getBytes(), 0, abPlainTxt, 0,
                    size);
        }
        abCipherTxt = new byte[lenPlainTxt];
        SEED_KISA.SeedRoundKey(aiRoundKey, abUserKey);
        for (int pos = 0; pos < lenPlainTxt; pos += 16) {
            System.arraycopy(abPlainTxt, pos, abPlainTxt16, 0, 16);
            SEED_KISA.SeedEncrypt(abPlainTxt16, aiRoundKey, abCipherTxt16);
            System.arraycopy(abCipherTxt16, 0, abCipherTxt, pos, 16);
        }

        String strByte = null;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lenPlainTxt; i++) {
            strByte = Integer.toHexString(0xff & abCipherTxt[i]);
            if (strByte.length() == 1)
                sb.append('0');
            sb.append(strByte);
        }

        return sb.toString();
    }

    public static String decryptBySeed(String strUserKey, String strCipherTxt)
            throws UnsupportedEncodingException {
        int lenCipherTxt = strCipherTxt.length() / 2;
        int aiRoundKey[] = new int[32];
        byte abUserKey[] = (byte[]) null;
        byte abPlainTxt[] = new byte[lenCipherTxt];
        byte abPlainTxt16[] = new byte[16];
        byte abCipherTxt[] = new byte[lenCipherTxt];
        byte abCipherTxt16[] = new byte[16];
        if (strUserKey == null || "".equals(strUserKey.trim())
                || "null".equals(strUserKey))
            abUserKey = "eastdg-SamlSsoSK".getBytes();
        else
            abUserKey = strUserKey.getBytes();
        for (int i = 0; i < lenCipherTxt; i++)
            abCipherTxt[i] = (byte) Integer.parseInt(
                    strCipherTxt.substring(i * 2, i * 2 + 2), 16);

        SEED_KISA.SeedRoundKey(aiRoundKey, abUserKey);
        for (int pos = 0; pos < lenCipherTxt; pos += 16) {
            System.arraycopy(abCipherTxt, pos, abCipherTxt16, 0, 16);
            SEED_KISA.SeedDecrypt(abCipherTxt16, aiRoundKey, abPlainTxt16);
            System.arraycopy(abPlainTxt16, 0, abPlainTxt, pos, 16);
        }

        return (new String(abPlainTxt)).trim();
    }
}
