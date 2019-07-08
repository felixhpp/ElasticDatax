package com.example.demo.core.config;

import org.elasticsearch.action.bulk.BackoffPolicy;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * elasticsearch配置信息
 *
 * @author felix
 */
@Configuration
public class ElasticsearchConfig {
    private static final Logger logger = LoggerFactory.getLogger("elasticsearch-server");

    /**
     * elk集群地址
     */
    @Value("${spring.elasticsearch.ip}")
    private String hostName;
    /**
     * 端口
     */
    @Value("${spring.elasticsearch.port}")
    private String port;
    /**
     * 集群名称
     */
    @Value("${spring.elasticsearch.cluster.name}")
    private String clusterName;

    /**
     * 连接池
     */
    @Value("${spring.elasticsearch.pool}")
    private String poolSize;

    @Bean
    public TransportClient init() {

        TransportClient transportClient = null;

        try {
            // 配置信息
            Settings esSetting = Settings.builder()
                    .put("cluster.name", clusterName)
                    //增加嗅探机制，找到ES集群,
                    // 如果设置为true, 必须设置elasticsearch.yml 中 network.host和http.port，配置外网访问的地址和port
                    .put("client.transport.sniff", true)
                    .put("thread_pool.search.size", Integer.parseInt(poolSize))//增加线程池个数，暂时设为5
                    .build();

            transportClient = TransportClient.builder().settings(esSetting).build();
            InetSocketTransportAddress inetSocketTransportAddress =
                    new InetSocketTransportAddress(InetAddress.getByName(hostName),
                            Integer.valueOf(port));
            transportClient.addTransportAddresses(inetSocketTransportAddress);
            logger.info("Setting server pool to a list of 1 servers: [{}]", hostName);
        } catch (Exception e) {
            logger.error("elasticsearch TransportClient create error!!!", e);
        }

        return transportClient;
    }

    @Bean
    public BulkProcessor bulkProcessor(TransportClient client) throws UnknownHostException {
        return BulkProcessor.builder(client, new BulkProcessor.Listener() {
            @Override
            public void beforeBulk(long l, BulkRequest bulkRequest) {
                //System.out.println("beforeBulk :" + l);
            }

            @Override
            public void afterBulk(long l, BulkRequest bulkRequest, BulkResponse bulkResponse) {
                System.out.println(bulkRequest.numberOfActions() + "data bulk finish");
                logger.info("[ {} ] data bulk finish. in {} milliseconds", bulkRequest.numberOfActions(), bulkResponse.getTook().getMillis());
            }

            @Override
            public void afterBulk(long l, BulkRequest bulkRequest, Throwable throwable) {
                System.out.println(bulkRequest.numberOfActions() + "data bulk failed, reason :" + throwable.getMessage());
                logger.error("{} data bulk failed,reason :{}", bulkRequest.numberOfActions(), throwable);
            }

        }).setBulkActions(20000)
                .setBulkSize(new ByteSizeValue(200, ByteSizeUnit.MB))
                .setFlushInterval(TimeValue.timeValueSeconds(5))
                .setConcurrentRequests(1)
                .setBackoffPolicy(BackoffPolicy.exponentialBackoff(TimeValue.timeValueMillis(100), 3))
                .build();
    }
}
