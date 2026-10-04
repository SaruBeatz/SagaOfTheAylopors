package com.example.sagaoftheaylopors.ml;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import com.example.sagaoftheaylopors.R;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps accentuation class labels to short UI descriptions (string resources).
 */
public final class AccentuationTexts {

    private static final Map<String, Integer> DESCRIPTION_RES = new HashMap<>();

    static {
        DESCRIPTION_RES.put("Гипертим", R.string.accentuation_desc_gipertym);
        DESCRIPTION_RES.put("Возбудимый", R.string.accentuation_desc_vozbudimy);
        DESCRIPTION_RES.put("Эмотив", R.string.accentuation_desc_emotiv);
        DESCRIPTION_RES.put("Педантичный", R.string.accentuation_desc_pedantichny);
        DESCRIPTION_RES.put("Тревожный", R.string.accentuation_desc_trevozhny);
        DESCRIPTION_RES.put("Циклотим", R.string.accentuation_desc_ciklotim);
        DESCRIPTION_RES.put("Демонстративный", R.string.accentuation_desc_demonstrativny);
        DESCRIPTION_RES.put("Неуравновешенный", R.string.accentuation_desc_neuravnoveshenny);
        DESCRIPTION_RES.put("Дистим", R.string.accentuation_desc_distim);
        DESCRIPTION_RES.put("Экзальтированный", R.string.accentuation_desc_ekzaltirovanny);
    }

    private AccentuationTexts() {
    }

    @StringRes
    public static int getDescriptionResId(@NonNull String label) {
        Integer resId = DESCRIPTION_RES.get(label);
        return resId != null ? resId : R.string.accentuation_desc_unknown;
    }
}
