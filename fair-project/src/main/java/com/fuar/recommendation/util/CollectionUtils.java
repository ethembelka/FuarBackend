package com.fuar.recommendation.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Entity koleksiyonları ile çalışmak için yardımcı sınıf
 */
public class CollectionUtils {
    
    /**
     * Set'i List'e dönüştürür
     * 
     * @param <T> Koleksiyon elemanı tipi
     * @param set Dönüştürülecek set
     * @return List tipinde sonuç
     */
    public static <T> List<T> toList(Set<T> set) {
        if (set == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(set);
    }
}
