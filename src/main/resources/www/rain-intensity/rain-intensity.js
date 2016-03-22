"use strict";

function RainIntensity() {

  var ID = Math.random().toFixed(10);
  var rainIntensitySlider;
  var isSliderActive = false;

  this.initializeSlider = function() {
    rainIntensitySlider = document.getElementById('rain-intensity');
    noUiSlider.create(rainIntensitySlider, {
      start: [0],
      orientation: 'vertical',
      direction: 'rtl',
      range: {
        'min': 0,
        'max': 100
      },
      pips: {
        mode: 'positions',
        values: [0,25,50,75,100],
        density: 4
      }
    });

    rainIntensitySlider.noUiSlider.on('update', function(values, handle) {
      if (isSliderActive) {
        eventBus.publish('rain.intensity.set', {
            'value': (values[handle] / 100.0)
          },
          {'id': ID});
      }
    });

    isSliderActive = true;
    whenSliderIsReady.completed();
  };

  this.updateRainIntensity = function(err, msg) {
    var intensityPercentage = 100.0 * msg.body.value;
    document.getElementById('rain-intensity-percentage').innerHTML = intensityPercentage.toFixed(0) + "%";
    if (msg.headers == null || msg.headers.id != ID) {
      isSliderActive = false;
      rainIntensitySlider.noUiSlider.set(intensityPercentage);
      isSliderActive = true;
    }
  };

  whenDomIsReady.thenDo(this.initializeSlider);
}