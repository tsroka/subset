angular.module('subsetUI').factory('$uploadService', ($q)->
  class UploadRequest
    constructor: (@file, @name, @url, @method) ->
      @deferred = $q.defer()
      @xhr = new XMLHttpRequest()

    onProgress: (evt) =>
      percent = if(evt.lengthComputable) then Math.round(evt.loaded * 100 / evt.total) else -1
      @deferred.notify(percent)

    onFailed: (evt) =>
      @deferred.reject(evt)
    onCompleted: (evt) =>
      @deferred.resolve(evt.target)

    execute: =>
      formData = new FormData
      formData.append(@name, @file)

      @xhr.upload.addEventListener('progress', @onProgress, false)
      @xhr.addEventListener("load", @onCompleted, false)
      @xhr.addEventListener("error", @onFailed, false)
      @xhr.addEventListener("abort", @onFailed, false)
      @xhr.open(@method, @url)
      @xhr.send(formData)


    promise: =>
      promise = @deferred.promise

      promise.success = (fn) ->
        promise.then(fn)
        return promise

      promise.error = (fn) ->
        promise.then(null, fn)
        return promise

      promise.progress = (fn) ->
        promise.then(null, null, fn)
        return promise

      promise.abort = =>
        @xhr.abort()

      return promise


  return  (file, name, url, method) ->
    request = new UploadRequest(file, name, url, method)
    request.execute()
    return request.promise()
)


class SubsetService


  constructor: (@$resource, @$q) ->
    ruleActions =
      create:
        method: 'PUT'

    @rules = @$resource('/rule/:name', null, ruleActions)
    @csv = @$resource('/csv/:name')


  uploadRule: (name, specification) =>
    deferred = @$q.defer()
    @rules.create({'name': name}, specification, @handleSucc(deferred), @handleFailure(deferred))
    return @promise(deferred)

  uploadCsv: (name, csvData) =>
    deferred = @$q.defer()
    @csv.save({'name': name}, csvData, @handleSucc(deferred), @handleFailure(deferred))
    return @promise(deferred)


  handleFailure: (deferred) =>
    (httpResponse) ->
      deferred.reject(httpResponse)

  handleSucc: (deferred) =>
    (value) ->
      deferred.resolve(value)

  promise: (deferred) =>
    promise = deferred.promise

    promise.success = (fn) ->
      promise.then(fn)
      return promise

    promise.error = (fn) ->
      promise.then(null, fn)
      return promise

    return promise


angular.module('subsetUI').service 'subsetService', ['$resource', '$q', SubsetService]