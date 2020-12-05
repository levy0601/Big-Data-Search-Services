package com.example.demo.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

public class ElasticsearchConfiguration {
    @Configuration
    public class ElasticsearchConfig {

        @Value("${elasticsearch.host}")
        private String elasticsearchHost;

        @Bean(destroyMethod = "close")
        public RestHighLevelClient restHighLevelClient() {

            RestHighLevelClient client = new RestHighLevelClient(
                    RestClient.builder(new HttpHost(elasticsearchHost)));

            return client;

        }


    }
}
