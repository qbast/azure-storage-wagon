/*
 * Copyright 2021 Jakub Stachowski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.qbast.azurewagon.abs;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.AzureCliCredentialBuilder;
import com.azure.identity.ChainedTokenCredentialBuilder;
import com.azure.identity.EnvironmentCredentialBuilder;
import com.azure.identity.ManagedIdentityCredentialBuilder;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.common.StorageSharedKeyCredential;
import org.apache.maven.wagon.authentication.AuthenticationInfo;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

public class AuthenticationHandler {
    private static final Logger LOGGER = Logger.getLogger(AuthenticationHandler.class.getName());

    private static final String ENDPOINT_TEMPLATE = "https://%s.blob.core.windows.net/";
    private static final ConcurrentMap<String, BlobServiceClient> builders = new ConcurrentHashMap<>();

    public static BlobServiceClient get(String accountName, final AuthenticationInfo authentication) {
        return builders.computeIfAbsent(accountName, (ac) -> createAuthenticatedBuilder(ac, authentication).buildClient());
    }


    private static BlobServiceClientBuilder createAuthenticatedBuilder(String accountName, AuthenticationInfo authentication) {
        BlobServiceClientBuilder builder = new BlobServiceClientBuilder().
                endpoint(String.format(ENDPOINT_TEMPLATE, accountName));

        if (authentication!=null) {
            builder.credential(new StorageSharedKeyCredential(authentication.getUserName(), authentication.getPassword()));
        } else {
            builder.credential(createAzureAdCredential());
        }
        LOGGER.finer(String.format("Created authenticated client for account %s", accountName));
        return builder;
    }

    private static TokenCredential createAzureAdCredential() {
        return new ChainedTokenCredentialBuilder().
                addAll(Arrays.asList(
                        new EnvironmentCredentialBuilder().build(),
                        new ManagedIdentityCredentialBuilder().build(),
                        new AzureCliCredentialBuilder().build()
                )).
                build();
    }


}
