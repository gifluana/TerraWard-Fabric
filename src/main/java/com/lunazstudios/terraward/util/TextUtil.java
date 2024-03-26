package com.lunazstudios.terraward.util;

import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextUtil {

    /**
     * Helper method to convert colored text using '&' into Mutable Text.
     */
    public static MutableText colorize(String message) {
        Pattern pattern = Pattern.compile("&([0-9a-fklmnor])");
        Matcher matcher = pattern.matcher(message);

        MutableText result = Text.empty();
        Style currentStyle = Style.EMPTY;
        int lastEnd = 0;

        while (matcher.find()) {
            if (matcher.start() > lastEnd) {
                String textPart = message.substring(lastEnd, matcher.start());
                result.append(Text.literal(textPart).setStyle(currentStyle));
            }

            lastEnd = matcher.end();

            char code = matcher.group(1).charAt(0);
            Formatting newFormat = ColorCodes.getFormattingByCode(code);
            if (newFormat.isColor()) {
                currentStyle = Style.EMPTY.withColor(newFormat);
            } else {
                currentStyle = currentStyle.withFormatting(newFormat);
            }
        }

        if (lastEnd < message.length()) {
            String textPart = message.substring(lastEnd);
            result.append(Text.literal(textPart).setStyle(currentStyle));
        }

        return result;
    }
}
