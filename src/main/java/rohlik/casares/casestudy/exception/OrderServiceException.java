package rohlik.casares.casestudy.exception;

public class OrderServiceException extends Exception {
    public OrderServiceException(Exception cause) {
        super(cause);
    }

    public OrderServiceException(String message) {
        super(message);
    }
}
