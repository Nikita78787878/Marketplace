package org.example.marketplace.event;

/**
 * Событие «товар изменился или удалён».
 *
 * Публикуется сервисом внутри транзакции, а доставляется ProductCacheEvictor'у
 * только после её успешного коммита — за отсрочку отвечает @TransactionalEventListener.
 *
 * Обычный record без аннотаций: с версии Spring 4.2 событием может быть любой объект,
 * наследовать ApplicationEvent не требуется. Несёт только id — слушателю больше нечего знать.
 */
public record ProductChangedEvent(Long productId) {
}
