package ec.edu.uteq.distribuidas;

import javax.crypto.*;
import javax.crypto.spec.*;
import java.security.*;
import java.util.Base64;

/**
 * Implementacion de cifrado simetrico AES-256-GCM.
 * GCM (Galois/Counter Mode) proporciona autenticacionintegrada (AEAD).
 * Es el modo recomendado para sistemas distribuidos en 2026.
 */
public class CifradoAES {
    private static final String ALGORITMO = "AES/GCM/NoPadding";
    private static final int BITS_CLAVE = 256;
    private static final int LONGITUD_IV = 12; // 96 bitsrecomendado para GCM
    private static final int BITS_TAG = 128; // tag deautenticacion GCM

    /**
     * Genera una clave AES-256 aleatoria y criptograficamentesegura.
     */
    public static SecretKey generarClave() throws NoSuchAlgorithmException {
        KeyGenerator generador = KeyGenerator.getInstance("AES");
        generador.init(BITS_CLAVE, new SecureRandom());
        return generador.generateKey();
    }

    /**
     * Cifra un mensaje con AES-256-GCM.
     *
     * @param clave clave AES-256
     * @return arreglo de bytes: [IV (12 bytes) | ciphertext+tag]
     */
    public static byte[] cifrar(String textoPlano, SecretKey clave) throws GeneralSecurityException {
        // Generar IV aleatorio unico por cada cifrado
        byte[] iv = new byte[LONGITUD_IV];
        new SecureRandom().nextBytes(iv);

        Cipher cifrador = Cipher.getInstance(ALGORITMO);
        GCMParameterSpec parametros = new GCMParameterSpec(BITS_TAG, iv);
        cifrador.init(Cipher.ENCRYPT_MODE, clave, parametros);

        byte[] ciphertext = cifrador.doFinal(textoPlano.getBytes(java.nio.charset.StandardCharsets.UTF_8));

        // Concatenar IV + ciphertext para transmision
        byte[] resultado = new byte[LONGITUD_IV + ciphertext.length];
        System.arraycopy(iv, 0, resultado, 0, LONGITUD_IV);
        System.arraycopy(ciphertext, 0, resultado, LONGITUD_IV,
                ciphertext.length);
        return resultado;
    }

    /**
     * Descifra un mensaje cifrado con AES-256-GCM.
     * Si el mensaje fue alterado, GCM lanzara AEADBadTagException.
     */
    public static String descifrar(byte[] mensajeCifrado, SecretKey clave) throws GeneralSecurityException {
        // Extraer IV de los primeros 12 bytes
        byte[] iv = new byte[LONGITUD_IV];
        System.arraycopy(mensajeCifrado, 0, iv, 0, LONGITUD_IV);
        byte[] ciphertext = new byte[mensajeCifrado.length - LONGITUD_IV];
        System.arraycopy(mensajeCifrado, LONGITUD_IV, ciphertext, 0, ciphertext.length);

        Cipher descifrador = Cipher.getInstance(ALGORITMO);
        GCMParameterSpec parametros = new GCMParameterSpec(BITS_TAG, iv);
        descifrador.init(Cipher.DECRYPT_MODE, clave, parametros);

        byte[] textoPlano = descifrador.doFinal(ciphertext);
        return new String(textoPlano, java.nio.charset.
                StandardCharsets.UTF_8);
    }

    /**
     * Convierte bytes a Base64 para transmision como texto.
     */
    public static String aBase64(byte[] datos) {
        return Base64.getEncoder().encodeToString(datos);
    }

    public static byte[] deBase64(String base64) {
        return Base64.getDecoder().decode(base64);
    }

    // Demo
    public static void main(String[] args) throws Exception {
        System.out.println("=== Demo AES-256-GCM ===\n");

        SecretKey clave = generarClave();
        System.out.println("Clave generada (Base64): " + aBase64(clave.getEncoded()).substring(0, 20) + "...");

        String mensajeOriginal = "Token JWT del usuario: eyJhbGciOiJSUzI1NiJ9...";
        System.out.println("Mensaje original: " + mensajeOriginal);

        // Cifrar
        byte[] cifrado = cifrar(mensajeOriginal, clave);
        System.out.println("Mensaje cifrado (Base64): " + aBase64(cifrado).substring(0, 40) + "...");

        // Descifrar
        String descifrado = descifrar(cifrado, clave);
        System.out.println("Mensaje descifrado: " + descifrado);

        System.out.println("\nIntegridad verificada: " + mensajeOriginal.equals(descifrado));

        // Simular alteracion del ciphertext
        System.out.println("\n---Simulando ataque de alteracion---");
        byte[] alterado = cifrado.clone();
        alterado[15] ^= 0xFF; // alterar un byte del ciphertext
        try {
            descifrar(alterado, clave);
        } catch (AEADBadTagException e) {
            System.out.println("ALERTA: Mensaje alterado detectado! " + e.getClass().getSimpleName());
        }
    }
}