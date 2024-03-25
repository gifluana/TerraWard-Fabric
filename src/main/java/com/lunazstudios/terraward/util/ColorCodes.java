package com.lunazstudios.terraward.util;

import net.minecraft.util.Formatting;

public enum ColorCodes {
    BLACK('0', Formatting.BLACK),
    DARK_BLUE('1', Formatting.DARK_BLUE),
    DARK_GREEN('2', Formatting.DARK_GREEN),
    DARK_AQUA('3', Formatting.DARK_AQUA),
    DARK_RED('4', Formatting.DARK_RED),
    DARK_PURPLE('5', Formatting.DARK_PURPLE),
    GOLD('6', Formatting.GOLD),
    GRAY('7', Formatting.GRAY),
    DARK_GRAY('8', Formatting.DARK_GRAY),
    BLUE('9', Formatting.BLUE),
    GREEN('a', Formatting.GREEN),
    AQUA('b', Formatting.AQUA),
    RED('c', Formatting.RED),
    LIGHT_PURPLE('d', Formatting.LIGHT_PURPLE),
    YELLOW('e', Formatting.YELLOW),
    WHITE('f', Formatting.WHITE),
    BOLD('l', Formatting.BOLD),
    UNDERLINE('n', Formatting.UNDERLINE),
    ITALIC('o', Formatting.ITALIC),
    STRIKETHROUGH('m', Formatting.STRIKETHROUGH),
    OBFUSCATED('k', Formatting.OBFUSCATED),
    RESET('r', Formatting.RESET);

    private final char code;
    private final Formatting formatting;

    ColorCodes(char code, Formatting formatting) {
        this.code = code;
        this.formatting = formatting;
    }

    public char getCode() {
        return this.code;
    }

    public Formatting getFormatting() {
        return this.formatting;
    }

    public static Formatting getFormattingByCode(char code) {
        for (ColorCodes value : ColorCodes.values()) {
            if (value.getCode() == code) {
                return value.getFormatting();
            }
        }
        return Formatting.WHITE;
    }
}
