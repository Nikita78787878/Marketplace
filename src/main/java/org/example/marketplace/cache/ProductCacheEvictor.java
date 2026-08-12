package org.example.marketplace.cache;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.marketplace.dto.product.ProductResponse;
import org.example.marketplace.event.ProductChangedEvent;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Инвалидация кеша товара.
 *
 * Вынесена из сервиса в отдельный слушатель намеренно: ProductServiceCache занимается
 * бизнес-логикой и не обязан знать, что где-то есть Redis. Он лишь сообщает факт
 * «товар изменился», а как на это реагировать — забота этого класса.
 */
@Component
@Slf4j
@AllArgsConstructor
public class ProductCacheEvictor {
    private final RedisTemplate<String, ProductResponse> redisTemplate;

    /**
     * AFTER_COMMIT здесь принципиален, а не для красоты.
     *
     * Если чистить кеш прямо в методе сервиса, удаление произойдёт ДО коммита: коммит
     * выполняет транзакционный прокси уже после выхода из метода. В этот зазор параллельный
     * GET увидит пустой кеш, прочитает из БД ещё не закоммиченные (то есть старые) данные
     * и запишет их обратно в Redis. Инвалидация отработает вхолостую, а протухшее значение
     * проживёт весь TTL.
     *
     * Полностью гонку это не убирает: между COMMIT и DEL остаются миллисекунды, в которые
     * читатель всё ещё может прогреть кеш старым значением. Абсолютной согласованности кеша
     * с БД не существует — TTL и есть страховка на этот случай.
     *
     * Ловушка: без активной транзакции @TransactionalEventListener не вызывается ВООБЩЕ,
     * молча и без предупреждений. Публиковать событие вне @Transactional бессмысленно.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onProductChanged(ProductChangedEvent event){
        String key = ProductCacheKeys.key(event.productId());

        try{
            redisTemplate.delete(key);
            log.debug("Инвалидировали кеш, key={}", key);
        }catch (Exception e){
            // Пробрасывать исключение отсюда нельзя: коммит уже прошёл, данные в БД сохранены.
            // Оно улетело бы вверх через processCommit и вернулось клиенту как 500 — тот решил бы,
            // что апдейт не удался, и повторил бы запрос впустую.
            // Недоступный кеш — не повод ронять запрос: ключ протухнет по TTL сам.
            log.error("Не удалось инвалидировать кеш, key={}", key, e);
        }
    }
}
