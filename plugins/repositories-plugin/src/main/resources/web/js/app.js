var repoConnectionApp = angular.module("repo-connection-app", [
  'ngRoute',
  'ngAnimate',
  'ngSanitize',
  'repoConnectionAppControllers'
]);

repoConnectionApp.config(['$routeProvider',
  function($routeProvider) {
    $routeProvider.
    when('/repository-selection', {
      templateUrl: 'repository-selection.html',
      controller: 'RepositorySelectionController'
    }).
    when('/pentaho-repository', {
      templateUrl: 'pentaho-repository.html',
      controller: 'PentahoRepositoryController'
    }).
    when('/pentaho-repository-2', {
      templateUrl: 'pentaho-repository-2.html',
      controller: 'PentahoRepositoryController'
    }).
    when('/kettle-database-repository', {
      templateUrl: 'kettle-database-repository.html',
      controller: 'KettleDatabaseRepositoryController'
    }).
    when('/kettle-file-repository', {
      templateUrl: 'kettle-file-repository.html',
      controller: 'KettleFileRepositoryController'
    }).
    otherwise({
      redirectTo: '/repository-selection'
    });
  }]).
  run(function($rootScope, $window) {
  $rootScope.slide = '';
  $rootScope.$on('$routeChangeStart', function() {
      $rootScope.back = function() {
          $rootScope.slide = 'to-right';
          $window.history.back();
      }
      $rootScope.next = function() {
          $rootScope.slide = 'to-left';
      }
    })
  });

repoConnectionApp.animation('.to-left', [function() {
  return {
    enter: function(element, doneFn) {
      $(element).css("left", $(window).width())
      $(element).animate({
        left: 0
      });
    },

    move: function(element, doneFn) {
    },

    leave: function(element, doneFn) {
      $(element).animate({
        left: -$(window).width()
      })
    }
  }
}]);

repoConnectionApp.animation('.to-right', [function() {
  return {
    enter: function(element, doneFn) {
      $(element).css("left", -$(window).width())
      $(element).animate({
        left: 0
      });
    },
    leave: function(element, doneFn) {
      $(element).animate({
        left: $(window).width()
      })
    }
  }
}]);

repoConnectionApp.directive('buttonSelect', function() {
    return function($scope, element) {
      element.bind('click', function() {
        var left = $(element).offset().left + $(element).outerWidth() / 2;
        $('#triangle-up').animate({
          left: left - $('#triangle-up').outerWidth() / 2
        }, 250, "linear");
      });
    }
});

repoConnectionApp.directive('onFinishRender', function ($timeout) {
    return {
        restrict: 'A',
        link: function (scope, element, attr) {
            if (scope.$last === true) {
                $timeout(function () {
                    scope.$emit('ngRepeatFinished');
                });
            }
        }
    }
});

repoConnectionApp.directive('eventFocus', function(focus) {
  return function(scope, elem, attr) {
    elem.on(attr.eventFocus, function() {
      focus(attr.eventFocusId);
    });

    // Removes bound events in the element itself
    // when the scope is destroyed
    scope.$on('$destroy', function() {
      elem.off(attr.eventFocus);
    });
  };
});
