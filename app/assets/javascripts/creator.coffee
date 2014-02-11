class CsvUploadController
  constructor: (@$scope, @$uploadService, @$rootScope, @$location) ->
    @$scope.csv = this
    @selectedFile = null
    @errorMessage = null
    @uploadInProgress = false
    @uploadProgress = 0

  onFileSelect: (files) =>
    @selectedFile = files[0] if(files? and files.length > 0)

  cancel: =>
    @uploadFuture.abort() if @uploadFuture?
    @selectedFile = null
    @uploadProgress = 0
    @uploadInProgress = false

  upload: =>
    if(!@selectedFile?)
      return false
    @errorMessage = null
    @uploadInProgress = true
    @uploadFuture = @$uploadService(@selectedFile, 'csvFile', "/parseCsv", "POST")
    .success(@onUploadSuccess)
    .error(@onUploadError)
    .progress(@onProgress)


  onProgress: (progress) =>
    @uploadProgress = progress

  onUploadError: (evt) =>
    console.log("Failed: #{evt}")
    @errorMessage = "Error occurred while communicating with server"
    @cancel()

  onUploadSuccess: (evt) =>
    try
      respObj = angular.fromJson(evt.response)
      if evt.status == 200
        console.info("Got OK with obj #{respObj}")
        @$rootScope.csvData = respObj
        @$location.path('/configColumns')
      else
        @errorMessage = if respObj['error']? then respObj['error'] else 'Unknown error occurred'
    catch e
      @errorMessage = "Error occurred while deserializing message"
    finally
      @cancel()

angular.module('subsetUI').controller 'csvDataUploadController',
  ['$scope', '$uploadService', '$rootScope', '$location', CsvUploadController]

class ColumnConfigController
  constructor: (@$scope, @$rootScope, @$location, @subsetService) ->
    @$scope.config = this
    @csv = @$rootScope.csvData
    #    @csv =
    #      headers: ['side', 'product', 'value']
    #      rows: [
    #        ['INT', 'IBM', 1.11],
    #        ['EXT', 'APPL', 2.22]
    #      ]

    @colStyles =
      active: []
      success: []
      info: []

    @sideColumn = null
    @side1Value = null


    @availGroupColumns = []
    @groupColumns = []

    @availAggrColumns = []
    @aggrColumns = []

    @onSideColumnChange()
    @onGroupColumnsChange()
    @$scope.$watch('config.sideColumn', @onSideColumnChange)
    @$scope.$watch('config.sideColumn', @calculateSideItemsCount)
    @$scope.$watch('config.side1Value', @calculateSideItemsCount)
    @$scope.$watchCollection('config.groupColumns', @onGroupColumnsChange)

    @$scope.$watch('config.sideColumn', @updateColStyles)
    @$scope.$watchCollection('config.groupColumns', @updateColStyles)
    @$scope.$watchCollection('config.aggrColumns', @updateColStyles)

  updateColStyles: =>
    @colStyles.active = [@indexOf(@sideColumn)]
    @colStyles.success = (@indexOf col for col in @groupColumns)
    @colStyles.info = (@indexOf col for col in @aggrColumns)

  colStylesFor: (index) =>
    name for name, indices of @colStyles when index in indices


  indexOf: (colName) =>
    if angular.isString(colName) and colName.length > 0 then @csv.content.headers.indexOf(colName) else -1

  calculateSideItemsCount: =>
    @side1ItemsCount = 0
    @side2ItemsCount = 0
    if(@sideColumn? and angular.isString(@side1Value) and @side1Value.length > 0)
      sideColNum = @indexOf(@sideColumn)
      for row in @csv.content.rows
        if(row[sideColNum] == @side1Value)
          @side1ItemsCount += 1
        else
          @side2ItemsCount += 1


  onSideColumnChange: =>
    @availGroupColumns = (col for col in @csv.content.headers when col isnt @sideColumn)
    @groupColumns = @intersect(@groupColumns, @availGroupColumns)
    @onGroupColumnsChange()

  onGroupColumnsChange: =>
    @availAggrColumns = (col for col in @availGroupColumns when !(col in @groupColumns))
    @aggrColumns = @intersect(@aggrColumns, @availAggrColumns)

  intersect: (a, b) ->
    (elem for elem in a when elem in b)

  nextStep: =>
    spec =
      sideField: @sideColumn
      side1Value: @side1Value
      groupFields: @groupColumns
      matchFields: @aggrColumns

    @subsetService.uploadRule('testRule', spec).success(@afterRuleUpload).error(@errorHandler)

  afterRuleUpload: (rule) =>
    console.info("Rule uploaded #{angular.toJson(rule, true)}")
    @csv.ruleId = rule.id
    @subsetService.uploadCsv(@csv.name, @csv).success(@afterCsvUpload).error(@errorHandler)

  afterCsvUpload: (csv) =>
    console.info("Rule uploaded #{angular.toJson(csv, true)}")

  errorHandler: (response) =>
    console.info("upload error #{angular.toJson(response.data)}")


angular.module('subsetUI').controller 'columnConfigController',
  ['$scope', '$rootScope', '$location', 'subsetService', ColumnConfigController]




