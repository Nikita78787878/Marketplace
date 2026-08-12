package org.example.marketplace.cache;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.marketplace.dto.product.ProductResponse;
import org.example.marketplace.event.ProductChangedEvent;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
@AllArgsConstructor
public class ProductCacheEvictor {
    private final RedisTemplate<String, ProductResponse> redisTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onProductChanged(ProductChangedEvent event){
        String key = ProductCacheKeys.key(event.productId());

        try{
            redisTemplate.delete(key);
            log.info("удалил из кеша");
        }catch (Exception e){
            log.error("Не удалось удалить из кеша", e);
        }


    }
}
