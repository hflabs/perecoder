package ru.hflabs.rcd.exception.constraint;

/**
 * Класс <class>IllegalPermissionsException</class> реализует исключительную ситуацию, возникающую при некорректных правах
 *
 * @see ConstraintException
 * @see ru.hflabs.rcd.model.Permissioned
 */
public class IllegalPermissionsException extends ConstraintException {

    private static final long serialVersionUID = -3783073427782351412L;

    public IllegalPermissionsException(String message) {
        super(message);
    }

    public static class IllegalWritePermissionsException extends IllegalPermissionsException {

        private static final long serialVersionUID = -8049030324058342739L;

        public IllegalWritePermissionsException(String message) {
            super(message);
        }
    }

    public static class IllegalReadPermissionsException extends IllegalPermissionsException {

        private static final long serialVersionUID = -2325006230663746736L;

        public IllegalReadPermissionsException(String message) {
            super(message);
        }
    }
}
