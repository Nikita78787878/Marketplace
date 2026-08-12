package org.example.marketplace.cache;

public final class ProductCacheKeys {
    public static final String PREFIX = "product:";

    private ProductCacheKeys() {}

    public static String key(Long id){
        return PREFIX + id;
    }
}
