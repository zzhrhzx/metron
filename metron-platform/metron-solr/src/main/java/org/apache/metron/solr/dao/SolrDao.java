/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.metron.solr.dao;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.metron.indexing.dao.AccessConfig;
import org.apache.metron.indexing.dao.ColumnMetadataDao;
import org.apache.metron.indexing.dao.IndexDao;
import org.apache.metron.indexing.dao.RetrieveLatestDao;
import org.apache.metron.indexing.dao.search.FieldType;
import org.apache.metron.indexing.dao.search.GetRequest;
import org.apache.metron.indexing.dao.search.GroupRequest;
import org.apache.metron.indexing.dao.search.GroupResponse;
import org.apache.metron.indexing.dao.search.InvalidSearchException;
import org.apache.metron.indexing.dao.search.SearchRequest;
import org.apache.metron.indexing.dao.search.SearchResponse;
import org.apache.metron.indexing.dao.update.CommentAddRemoveRequest;
import org.apache.metron.indexing.dao.update.Document;
import org.apache.metron.indexing.dao.update.OriginalNotFoundException;
import org.apache.metron.indexing.dao.update.PatchRequest;
import org.apache.metron.solr.client.SolrClientFactory;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpClientUtil;
import org.apache.solr.client.solrj.impl.Krb5HttpClientConfigurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SolrDao implements IndexDao {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  public static final String ROOT_FIELD = "_root_";
  public static final String VERSION_FIELD = "_version_";

  private transient SolrClient client;
  private SolrSearchDao solrSearchDao;
  private SolrUpdateDao solrUpdateDao;
  private SolrRetrieveLatestDao solrRetrieveLatestDao;
  private ColumnMetadataDao solrColumnMetadataDao;

  private AccessConfig accessConfig;

  protected SolrDao(SolrClient client,
      AccessConfig config,
      SolrSearchDao solrSearchDao,
      SolrUpdateDao solrUpdateDao,
      SolrRetrieveLatestDao retrieveLatestDao,
      SolrColumnMetadataDao solrColumnMetadataDao) {
    this.client = client;
    this.accessConfig = config;
    this.solrSearchDao = solrSearchDao;
    this.solrUpdateDao = solrUpdateDao;
    this.solrRetrieveLatestDao = retrieveLatestDao;
    this.solrColumnMetadataDao = solrColumnMetadataDao;
  }

  public SolrDao() {
    //uninitialized.
  }

  @Override
  public void init(AccessConfig config) {
    if (config.getKerberosEnabled()) {
      enableKerberos();
    }
    if (this.client == null) {
      this.accessConfig = config;
      this.client = SolrClientFactory.create(config.getGlobalConfigSupplier().get());
      this.solrSearchDao = new SolrSearchDao(this.client, this.accessConfig);
      this.solrRetrieveLatestDao = new SolrRetrieveLatestDao(this.client, this.accessConfig);
      this.solrUpdateDao = new SolrUpdateDao(this.client, this.solrRetrieveLatestDao, this.accessConfig);
      this.solrColumnMetadataDao = new SolrColumnMetadataDao(this.client);
    }
  }

  public Optional<String> getIndex(String sensorName, Optional<String> index) {
    if (index.isPresent()) {
      return index;
    } else {
      String realIndex = accessConfig.getIndexSupplier().apply(sensorName);
      return Optional.ofNullable(realIndex);
    }
  }

  @Override
  public SearchResponse search(SearchRequest searchRequest) throws InvalidSearchException {
    return this.solrSearchDao.search(searchRequest);
  }

  @Override
  public GroupResponse group(GroupRequest groupRequest) throws InvalidSearchException {
    return this.solrSearchDao.group(groupRequest);
  }

  @Override
  public Document getLatest(String guid, String sensorType) throws IOException {
    return this.solrRetrieveLatestDao.getLatest(guid, sensorType);
  }

  @Override
  public Iterable<Document> getAllLatest(List<GetRequest> getRequests) throws IOException {
    return this.solrRetrieveLatestDao.getAllLatest(getRequests);
  }

  @Override
  public Document update(Document update, Optional<String> index) throws IOException {
    return this.solrUpdateDao.update(update, index);
  }

  @Override
  public Map<Document, Optional<String>> batchUpdate(Map<Document, Optional<String>> updates) throws IOException {
    return this.solrUpdateDao.batchUpdate(updates);
  }

  @Override
  public Document addCommentToAlert(CommentAddRemoveRequest request) throws IOException {
    return this.solrUpdateDao.addCommentToAlert(request);
  }

  @Override
  public Document removeCommentFromAlert(CommentAddRemoveRequest request) throws IOException {
    return this.solrUpdateDao.removeCommentFromAlert(request);
  }

  @Override
  public Document patch(RetrieveLatestDao retrieveLatestDao, PatchRequest request,
      Optional<Long> timestamp)
      throws OriginalNotFoundException, IOException {
    return solrUpdateDao.patch(retrieveLatestDao, request, timestamp);
  }

  @Override
  public Map<String, FieldType> getColumnMetadata(List<String> indices) throws IOException {
    return this.solrColumnMetadataDao.getColumnMetadata(indices);
  }

  @Override
  public Document addCommentToAlert(CommentAddRemoveRequest request, Document latest)
      throws IOException {
    return this.solrUpdateDao.addCommentToAlert(request, latest);
  }

  @Override
  public Document removeCommentFromAlert(CommentAddRemoveRequest request, Document latest)
      throws IOException {
    return this.solrUpdateDao.removeCommentFromAlert(request, latest);
  }

  void enableKerberos() {
    HttpClientUtil.addConfigurer(new Krb5HttpClientConfigurer());
  }

  public SolrSearchDao getSolrSearchDao() {
    return solrSearchDao;
  }

  public SolrUpdateDao getSolrUpdateDao() {
    return solrUpdateDao;
  }
}
