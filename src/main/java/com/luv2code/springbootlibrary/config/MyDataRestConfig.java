package com.luv2code.springbootlibrary.config;

import com.luv2code.springbootlibrary.entity.Book;
import com.luv2code.springbootlibrary.entity.Review;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

@Configuration
public class MyDataRestConfig implements RepositoryRestConfigurer {

    @Override
    public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config, CorsRegistry cors) {
        // Base path
        config.setBasePath("/api");

        // Exposer les IDs
        config.exposeIdsFor(Book.class);
        config.exposeIdsFor(Review.class);

        // Désactiver certaines méthodes
        HttpMethod[] unsupportedActions = {
                HttpMethod.POST,
                HttpMethod.PATCH,
                HttpMethod.DELETE,
                HttpMethod.PUT
        };

        disableHttpMethods(Book.class, config, unsupportedActions);
        disableHttpMethods(Review.class, config, unsupportedActions);
    }

    private void disableHttpMethods(Class<?> entityClass, RepositoryRestConfiguration config,
                                    HttpMethod[] unsupportedMethods) {
        config.getExposureConfiguration()
                .forDomainType(entityClass)
                .withItemExposure((metadata, httpMethods) ->
                        httpMethods.disable(unsupportedMethods))
                .withCollectionExposure((metadata, httpMethods) ->
                        httpMethods.disable(unsupportedMethods));
    }
}