package com.example.demo.config;

import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.bulk.*;
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
import java.util.List;

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
                if(bulkResponse.hasFailures()){
                    StringBuilder sb = new StringBuilder();
                    sb.append("failure in bulk execution:");
                    int length = bulkResponse.getItems().length;
                    int curFlg = 0;
                    for(int i = 0; i < length; ++i) {
                        BulkItemResponse response = bulkResponse.getItems()[i];
                        if (response.isFailed()) {
                            curFlg++;
                            if(curFlg == 1){
                                sb.append("\n[").append(i)
                                        .append("]: index [")
                                        .append(response.getIndex()).append("], type [")
                                        .append(response.getType()).append("], id [").append(response.getId())
                                        .append("], message [").append(response.getFailureMessage())
                                        .append("].");
                            }
                        }
                    }
                    sb.append("[ failed total: ").append(curFlg).append("]");
                    logger.error(sb.toString());
                }
            }

            @Override
            public void afterBulk(long l, BulkRequest bulkRequest, Throwable throwable) {
                System.out.println(bulkRequest.numberOfActions() + "data bulk failed, reason :" + throwable);
                logger.error("[{}] data bulk failed,reason :{}", bulkRequest.numberOfActions(), throwable);
                List<ActionRequest> requests = bulkRequest.requests();
                StringBuilder sb = new StringBuilder();
                sb.append("failure in bulk execution:");
                for (int i = 0; i<requests.size();i++){
                    // 日志记录失败的第一条，便于查找原因
                    ActionRequest actionRequest = requests.get(i);
                    sb.append(actionRequest.toString());
                    if(i == 1){
                        break;
                    }
                }
                logger.error(sb.toString());
            }

        }).setBulkActions(10000)
                .setBulkSize(new ByteSizeValue(100, ByteSizeUnit.MB))
                // 每60s提交一次
                .setFlushInterval(TimeValue.timeValueSeconds(60))
                // 异步执行
                .setConcurrentRequests(6)
                // 设置退避, 100ms后执行, 最大请求3次
                .setBackoffPolicy(BackoffPolicy.exponentialBackoff(TimeValue.timeValueMillis(100), 1))
                .build();
    }
}
