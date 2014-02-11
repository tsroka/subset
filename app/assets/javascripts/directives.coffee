angular.module('subsetUI').directive('fileUpload', ->
  {
  scope: { 'onFileSelect': '&onFileSelect' }
  link: (scope, element, attributes) ->
    element.bind('change', ->
      scope.$apply(=>
        scope.onFileSelect()(this.files)
      )
    )
  }
)

angular.module('subsetUI').directive('filteredSelect', ->
  scope:
    multiple: '@'
    selectedItems: '=?'
    selectedItem: '=?'
    items: '='
  restrict: 'E'
  replace: true
  templateUrl: '/assets/partials/filteredSelect.html'
  controller: ($scope) ->
    $scope.selectedItems = [] if not angular.isArray($scope.selectedItems)

    $scope.isSelected = (item) ->
      item in $scope.selectedItems

    $scope.itemToggle = (item) ->
      if !($scope.isSelected(item))
        if $scope.multiple?
          $scope.selectedItems.push(item)
        else
          $scope.selectedItems = [ item ]
        $scope.selectedItem = item
      else
        $scope.selectedItems = (i for i in $scope.selectedItems when i isnt item)
        $scope.selectedItem = null


)

