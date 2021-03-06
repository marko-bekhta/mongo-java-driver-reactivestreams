/*
 * Copyright 2016 MongoDB, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mongodb.reactivestreams.client.internal

import com.mongodb.ReadConcern
import com.mongodb.ReadPreference
import com.mongodb.WriteConcern
import com.mongodb.async.client.gridfs.GridFSBucket as WrappedGridFSBucket
import com.mongodb.async.client.gridfs.GridFSDownloadStream
import com.mongodb.async.client.gridfs.GridFSFindIterable
import com.mongodb.async.client.gridfs.GridFSUploadStream
import com.mongodb.client.gridfs.model.GridFSDownloadOptions
import com.mongodb.client.gridfs.model.GridFSUploadOptions
import com.mongodb.reactivestreams.client.gridfs.AsyncInputStream
import com.mongodb.reactivestreams.client.gridfs.AsyncOutputStream
import com.mongodb.reactivestreams.client.gridfs.GridFSBucket
import org.bson.BsonObjectId
import org.bson.Document
import org.reactivestreams.Subscriber
import spock.lang.Specification

class GridFSBucketImplSpecification extends Specification {

    def subscriber = Stub(Subscriber) {
        onSubscribe(_) >> { args -> args[0].request(100) }
    }

    def 'should have the same methods as the wrapped GridFSBucket'() {
        given:
        def wrapped = (WrappedGridFSBucket.methods*.name).sort()
        def local = (GridFSBucket.methods*.name).sort()

        expect:
        wrapped == local
    }

    def 'should call the underlying GridFSBucket when getting bucket meta data'() {
        given:
        def wrapped = Mock(WrappedGridFSBucket)
        def bucket = new GridFSBucketImpl(wrapped)

        when:
        bucket.getBucketName()

        then:
        1 * wrapped.getBucketName()

        when:
        bucket.getChunkSizeBytes()

        then:
        1 * wrapped.getChunkSizeBytes()

        when:
        bucket.getWriteConcern()

        then:
        1 * wrapped.getWriteConcern()

        when:
        bucket.getReadPreference()

        then:
        1 * wrapped.getReadPreference()

        when:
        bucket.getReadConcern()

        then:
        1 * wrapped.getReadConcern()
    }

    def 'should call the underlying GridFSBucket when adjusting settings'() {
        given:
        def chunkSizeBytes = 1
        def writeConcern = WriteConcern.MAJORITY
        def readPreference = ReadPreference.secondaryPreferred()
        def readConcern = ReadConcern.MAJORITY

        def wrapped = Mock(WrappedGridFSBucket)
        def bucket = new GridFSBucketImpl(wrapped)

        when:
        bucket.withChunkSizeBytes(chunkSizeBytes)

        then:
        1 * wrapped.withChunkSizeBytes(chunkSizeBytes) >> wrapped

        when:
        bucket.withWriteConcern(writeConcern)

        then:
        1 * wrapped.withWriteConcern(writeConcern) >> wrapped

        when:
        bucket.withReadPreference(readPreference)

        then:
        1 * wrapped.withReadPreference(readPreference) >> wrapped

        when:
        bucket.withReadConcern(readConcern)

        then:
        1 * wrapped.withReadConcern(readConcern) >> wrapped
    }

    def 'should call the wrapped openUploadStream'() {
        given:
        def filename = 'filename'
        def options = new GridFSUploadOptions()
        def fileId = new BsonObjectId()
        def uploadStream = Stub(GridFSUploadStream)
        def wrapped = Mock(WrappedGridFSBucket)
        def bucket = new GridFSBucketImpl(wrapped)

        when:
        bucket.openUploadStream(filename)

        then:
        1 * wrapped.openUploadStream(filename) >> uploadStream

        when:
        bucket.openUploadStream(filename, options)

        then:
        1 * wrapped.openUploadStream(filename, options) >> uploadStream

        when:
        bucket.openUploadStream(fileId, filename)

        then:
        1 * wrapped.openUploadStream(fileId, filename) >> uploadStream

        when:
        bucket.openUploadStream(fileId, filename, options)

        then:
        1 * wrapped.openUploadStream(fileId, filename, options) >> uploadStream
    }

    def 'should call the wrapped uploadFromStream'() {
        given:
        def filename = 'filename'
        def options = new GridFSUploadOptions()
        def fileId = new BsonObjectId()
        def source = Stub(AsyncInputStream)

        def wrapped = Mock(WrappedGridFSBucket)
        def bucket = new GridFSBucketImpl(wrapped)

        when:
        def publisher = bucket.uploadFromStream(filename, source)

        then:
        0 * wrapped.uploadFromStream(filename, _, _)

        when:
        publisher.subscribe(subscriber)

        then:
        1 * wrapped.uploadFromStream(filename, _, _)

        when:
        publisher = bucket.uploadFromStream(filename, source, options)

        then:
        0 * wrapped.uploadFromStream(filename, _, options, _)

        when:
        publisher.subscribe(subscriber)

        then:
        1 * wrapped.uploadFromStream(filename, _, options, _)

        when:
        publisher = bucket.uploadFromStream(fileId, filename, source)

        then:
        0 * wrapped.uploadFromStream(fileId, filename, _, _)

        when:
        publisher.subscribe(subscriber)

        then:
        1 * wrapped.uploadFromStream(fileId, filename, _, _)

        when:
        publisher = bucket.uploadFromStream(fileId, filename, source, options)

        then:
        0 * wrapped.uploadFromStream(fileId, filename, _, options, _)

        when:
        publisher.subscribe(subscriber)

        then:
        1 * wrapped.uploadFromStream(fileId, filename, _, options, _)
    }

    def 'should call the wrapped openDownloadStream'() {
        given:
        def filename = 'filename'
        def options = new GridFSDownloadOptions()
        def fileId = new BsonObjectId()
        def downloadStream = Stub(GridFSDownloadStream)
        def wrapped = Mock(WrappedGridFSBucket)
        def bucket = new GridFSBucketImpl(wrapped)

        when:
        bucket.openDownloadStream(fileId)

        then:
        1 * wrapped.openDownloadStream(fileId) >> downloadStream

        when:
        bucket.openDownloadStream(fileId.getValue())

        then:
        1 * wrapped.openDownloadStream(fileId.getValue()) >> downloadStream

        when:
        bucket.openDownloadStream(filename)

        then:
        1 * wrapped.openDownloadStream(filename) >> downloadStream

        when:
        bucket.openDownloadStream(filename, options)

        then:
        1 * wrapped.openDownloadStream(filename, options) >> downloadStream
    }

    def 'should call the wrapped downloadToStream'() {
        given:
        def filename = 'filename'
        def options = new GridFSDownloadOptions()
        def fileId = new BsonObjectId()
        def destination = Stub(AsyncOutputStream)

        def wrapped = Mock(WrappedGridFSBucket)
        def bucket = new GridFSBucketImpl(wrapped)

        when:
        def publisher = bucket.downloadToStream(fileId, destination)

        then:
        0 * wrapped.downloadToStream(fileId, _, _)

        when:
        publisher.subscribe(subscriber)

        then:
        1 * wrapped.downloadToStream(fileId, _, _)

        when:
        publisher = bucket.downloadToStream(fileId.getValue(), destination)

        then:
        0 * wrapped.downloadToStream(fileId.getValue(), _, _)

        when:
        publisher.subscribe(subscriber)

        then:
        1 * wrapped.downloadToStream(fileId.getValue(), _, _)

        when:
        publisher = bucket.downloadToStream(filename, destination)

        then:
        0 * wrapped.downloadToStream(filename, _, _)

        when:
        publisher.subscribe(subscriber)

        then:
        1 * wrapped.downloadToStream(filename, _, _)

        when:
        publisher = bucket.downloadToStream(filename, destination, options)

        then:
        0 * wrapped.downloadToStream(filename, _, options, _)

        when:
        publisher.subscribe(subscriber)

        then:
        1 * wrapped.downloadToStream(filename, _, options, _)
    }

    def 'should call the underlying find method'() {
        given:
        def filter = new Document('filter', 2)
        def findIterable = Stub(GridFSFindIterable)
        def wrapped = Mock(WrappedGridFSBucket)
        def bucket = new GridFSBucketImpl(wrapped)

        when:
        bucket.find()

        then:
        1 * wrapped.find() >> findIterable

        when:
        bucket.find(filter)

        then:
        1 * wrapped.find(filter) >> findIterable
    }

    def 'should call the underlying delete method'() {
        given:
        def fileId = new BsonObjectId()
        def wrapped = Mock(WrappedGridFSBucket)
        def bucket = new GridFSBucketImpl(wrapped)

        when:
        def publisher = bucket.delete(fileId)

        then:
        0 * wrapped.delete(_, _)

        when:
        publisher.subscribe(subscriber)

        then:
        1 * wrapped.delete(fileId, _)

        when:
        publisher = bucket.delete(fileId.getValue())

        then:
        0 * wrapped.delete(_, _)

        when:
        publisher.subscribe(subscriber)

        then:
        1 * wrapped.delete(fileId.getValue(), _)
    }

    def 'should call the underlying rename method'() {
        given:
        def fileId = new BsonObjectId()
        def newFilename = 'newFilename'
        def wrapped = Mock(WrappedGridFSBucket)
        def bucket = new GridFSBucketImpl(wrapped)

        when:
        def publisher = bucket.rename(fileId, newFilename)

        then:
        0 * wrapped.rename(_, _)

        when:
        publisher.subscribe(subscriber)

        then:
        1 * wrapped.rename(fileId, newFilename, _)

        when:
        publisher = bucket.rename(fileId.getValue(), newFilename)

        then:
        0 * wrapped.rename(_, _)

        when:
        publisher.subscribe(subscriber)

        then:
        1 * wrapped.rename(fileId.getValue(), newFilename, _)
    }

    def 'should call the underlying drop method'() {
        given:
        def wrapped = Mock(WrappedGridFSBucket)
        def bucket = new GridFSBucketImpl(wrapped)

        when:
        def publisher = bucket.drop()

        then:
        0 * wrapped.drop(_)

        when:
        publisher.subscribe(subscriber)

        then:
        1 * wrapped.drop(_)
    }

}
