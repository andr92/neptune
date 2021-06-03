package ru.tinkoff.qa.neptune.rabbit.mq.function.delete.queue;

import com.rabbitmq.client.Channel;
import ru.tinkoff.qa.neptune.core.api.steps.SequentialActionSupplier;
import ru.tinkoff.qa.neptune.core.api.steps.annotations.Description;
import ru.tinkoff.qa.neptune.core.api.steps.annotations.DescriptionFragment;
import ru.tinkoff.qa.neptune.rabbit.mq.RabbitMqStepContext;

import java.io.IOException;

import static com.google.common.base.Preconditions.checkArgument;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class RabbitMqQueueDeleteSupplier extends SequentialActionSupplier<RabbitMqStepContext, Channel, RabbitMqQueueDeleteSupplier> {
    private final String queue;
    private ParametersForDelete parametersForDelete;

    protected RabbitMqQueueDeleteSupplier(String queue) {
        super();
        checkArgument(isNotBlank(queue));
        this.queue = queue;
        performOn(RabbitMqStepContext::getChannel);
    }
    @Description("Delete a queue - {queue}")
    public static RabbitMqQueueDeleteSupplier deleteQueue(@DescriptionFragment("queue") String queue) {
        return new RabbitMqQueueDeleteSupplier(queue);
    }

    public RabbitMqQueueDeleteSupplier setParametersForDelete(ParametersForDelete parametersForDelete) {
        this.parametersForDelete = parametersForDelete;
        return this;
    }

    @Override
    protected void howToPerform(Channel value) {
        try {
            if (parametersForDelete == null){
                value.queueDelete(queue);
            }
            value.queueDelete(queue,parametersForDelete.isIfUnused(), parametersForDelete.isIfEmpty());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
