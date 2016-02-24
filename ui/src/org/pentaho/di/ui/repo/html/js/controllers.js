var repoConnectionAppControllers = angular.module('repoConnectionAppControllers', []);

repoConnectionAppControllers.controller("RepositorySelectionController", function($scope, repoModel) {
  $scope.repoModel = repoModel;
  $scope.selectRepository = function(repository) {
    repoModel.selectedRepository = repository;
  }
  $scope.close = function() {
    close();
  }
  $scope.$on('ngRepeatFinished', function(ngRepeatFinishedEvent) {
    var selectedButton = $("#button-"+repoModel.selectedRepository.id);
    var left = selectedButton.offset().left + selectedButton.outerWidth() / 2;
    left -= $("#display-box").offset().left;
    $('#triangle-up').css("left", left - $('#triangle-up').outerWidth() / 2);
  });
});

repoConnectionAppControllers.controller("PentahoRepositoryController", function($scope, $location, pentahoRepositoryModel) {
  $scope.pentahoRepositoryModel = pentahoRepositoryModel;
  $scope.finish = function() {
    try {
      var repo = {
        "index": 1,
        "displayName": pentahoRepositoryModel.displayName,
        "description": pentahoRepositoryModel.description,
        "url": pentahoRepositoryModel.url
      }
      if (createPentahoRepo(JSON.stringify(repo))) {
        $location.path("/pentaho-repository-2");
      }
    } catch(e) {
      alert(e);
    }
  };
  $scope.close = function() {
    close();
  }
})

repoConnectionAppControllers.controller("KettleDatabaseRepositoryController", function() {

});

repoConnectionAppControllers.controller("KettleFileRepositoryController", function() {

});
