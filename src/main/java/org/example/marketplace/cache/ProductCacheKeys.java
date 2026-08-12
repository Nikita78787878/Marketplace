package org.example.marketplace.cache;

/**
 * Единственное место, где собирается ключ кеша товара.
 *
 * Ключ нужен в двух разных классах: ProductServiceCache его пишет, ProductCacheEvictor удаляет.
 * Если они разъедутся (например, "product:" против "products:"), не будет ни ошибки,
 * ни исключения — SET положит значение по одному ключу, DEL пойдёт по другому,
 * и кеш просто молча перестанет инвалидироваться. Такие баги ищутся неделями,
 * поэтому источник ключа ровно один.
 */
public final class ProductCacheKeys {
    public static final String PREFIX = "product:";

    private ProductCacheKeys() {}

    public static String key(Long id){
        return PREFIX + id;
    }
}
