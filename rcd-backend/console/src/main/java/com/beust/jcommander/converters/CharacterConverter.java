package com.beust.jcommander.converters;

import com.beust.jcommander.ParameterException;

/**
 * Класс <class>CharacterConverter</class> реализует конвертер из строки в {@link Character}
 *
 * @author Nazin Alexander
 */
public class CharacterConverter extends BaseConverter<Character> {

    public CharacterConverter(String optionName) {
        super(optionName);
    }

    @Override
    public Character convert(String value) {
        if (value != null && value.length() == 1) {
            return value.charAt(0);
        } else {
            throw new ParameterException(getErrorString(value, "a char"));
        }
    }
}
