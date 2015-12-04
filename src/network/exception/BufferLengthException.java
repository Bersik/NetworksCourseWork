package network.exception;

/**
 * Created on 8:57 01.12.2015
 *
 * @author Bersik
 */

public class BufferLengthException extends Throwable {
    @Override
    public String getMessage() {
        return "помилка: буфер переповнений";
    }
}
