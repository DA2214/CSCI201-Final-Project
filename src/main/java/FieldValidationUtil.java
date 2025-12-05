import java.util.regex.Pattern;

public abstract class FieldValidationUtil {
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    private FieldValidationUtil() {}

    public static boolean IsUsernameValid(String username) {
        return username != null && !username.isEmpty();
    }

    public static boolean IsEmailValid(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }

        return EMAIL_PATTERN.matcher(email).matches();
    }
}
