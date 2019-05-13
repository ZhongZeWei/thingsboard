/**
 * Copyright © 2016-2019 The Thingsboard Authors
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
package org.thingsboard.server.dao;

import com.google.common.util.concurrent.ListenableFuture;
import org.thingsboard.server.common.data.id.TenantId;

import java.util.List;
import java.util.UUID;

/**
 * dao类接口
 * @param <T>
 */
public interface Dao<T> {


    //根据租客id查找
    List<T> find(TenantId tenantId);

    //根据租客id，UUID查找
    T findById(TenantId tenantId, UUID id);
    //根据租客id，UUID异步查找
    ListenableFuture<T> findByIdAsync(TenantId tenantId, UUID id);

    //根据租客id保存数据
    T save(TenantId tenantId, T t);

    //根据租客id，UUID删除数据
    boolean removeById(TenantId tenantId, UUID id);

}
