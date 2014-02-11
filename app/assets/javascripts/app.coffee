subset = window.angular.module('subsetUI', ['ngRoute', 'ngResource'])



subset.config(['$routeProvider', ($routeProvider) ->
  $routeProvider.when '/uploadCsv',
    templateUrl: '/assets/partials/creatorUploadCsv.html'
    controller: 'csvDataUploadController'
  .when '/configColumns',
      templateUrl: '/assets/partials/creatorConfigColumns.html'
      controller: 'columnConfigController'
  .otherwise
      redirectTo: '/uploadCsv'
])

