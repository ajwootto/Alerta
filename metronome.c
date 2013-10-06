#include "pebble_os.h"
#include "pebble_app.h"
#include "pebble_fonts.h"
#include "mini-printf.h"


#define MY_UUID { 0x6D, 0x76, 0x59, 0xAA, 0x74, 0x63, 0x40, 0x1E, 0xA9, 0xBE, 0x1E, 0x88, 0xE8, 0x65, 0xB8, 0xB5 }
PBL_APP_INFO(MY_UUID,
             "Metronome", "Acme",
             1, 0, /* App version */
             DEFAULT_MENU_ICON,
             APP_INFO_STANDARD_APP);

Window window;

TextLayer timeText;
TextLayer tempoText;

AppTimerHandle timer_handle;

#define COOKIE_MY_TIMER 8989

int error = 0;
double tempo = 60.0;
int selectedTime = 4;
int timeCounter = 0;

int times[5] = {1, 2, 3, 4, 8};
int timeIndex = 3;
char str[20];

int interval;

void select_single_click_handler(ClickRecognizerRef recognizer, Window *window);
void up_single_click_handler(ClickRecognizerRef recognizer, Window *window);
void down_single_click_handler(ClickRecognizerRef recognizer, Window *window);

void config_provider(ClickConfig **config, Window *window) {
  // See ui/click.h for more information and default values.

  // single click / repeat-on-hold config:
  config[BUTTON_ID_SELECT]->click.handler = (ClickHandler) select_single_click_handler;
  config[BUTTON_ID_SELECT]->click.repeat_interval_ms = 100; // "hold-to-repeat" gets overridden if there's a long click handler configured!

  config[BUTTON_ID_UP]->click.handler = (ClickHandler) up_single_click_handler;
  config[BUTTON_ID_DOWN]->click.handler = (ClickHandler) down_single_click_handler;

  config[BUTTON_ID_UP]->click.repeat_interval_ms = 100;
  config[BUTTON_ID_DOWN]->click.repeat_interval_ms = 100;

  (void)window;
}

void select_single_click_handler(ClickRecognizerRef recognizer, Window *window) {
  timeIndex = timeIndex + 1;
  if (timeIndex > 5) {
    timeIndex = 0;
  }
  selectedTime = times[timeIndex];
  mini_snprintf(str, 20, "%d", selectedTime);
  text_layer_set_text(&tempoText, str);

}

void up_single_click_handler(ClickRecognizerRef recognizer, Window *window) {
  tempo = tempo + 1;
  mini_snprintf(str, 20, "%d", (int) tempo);
  text_layer_set_text(&tempoText, str);

}

void down_single_click_handler(ClickRecognizerRef recognizer, Window *window) {
  tempo = tempo - 1;
  mini_snprintf(str, 20, "%d", (int) tempo);
  text_layer_set_text(&tempoText, str);

}

void handle_timer(AppContextRef ctx, AppTimerHandle handle, uint32_t cookie) {

  if (cookie == COOKIE_MY_TIMER) {
      interval = 60.0 / tempo * 1000.0 / (selectedTime / 2);
      uint32_t segments[] = { interval, interval ,interval / 2,interval * 1.5,interval / 2,interval * 1.5,interval/2,interval*1.5, 1};
      VibePattern pat = {
        .durations = segments,
        .num_segments = ARRAY_LENGTH(segments),
      };
      vibes_enqueue_custom_pattern(pat);
      timer_handle = app_timer_send_event(ctx, 1000 - interval * 1.5 /* milliseconds */, COOKIE_MY_TIMER);
  }
  // If you want the timer to run again you need to call `app_timer_send_event()`
  // again here.
}

void handle_init(AppContextRef ctx) {

  window_init(&window, "Window Name");
  window_stack_push(&window, true /* Animated */);  
  text_layer_init(&tempoText, GRect(0, 65, 144, 30));
  text_layer_set_text_alignment(&tempoText, GTextAlignmentCenter);
  text_layer_set_font(&tempoText, fonts_get_system_font(FONT_KEY_BITHAM_30_BLACK));
  layer_add_child(&window.layer, &tempoText.layer);
  text_layer_set_text(&tempoText, "60");
  window_set_click_config_provider(&window, (ClickConfigProvider) config_provider);
  
  timer_handle = app_timer_send_event(ctx, 60 / tempo * (1000) /* milliseconds */, COOKIE_MY_TIMER);

}

void handle_tick(AppContextRef ctx, PebbleTickEvent *event) {
 
}


void pbl_main(void *params) {

  PebbleAppHandlers handlers = {
    .init_handler = &handle_init,
    .tick_info = {
     .tick_handler = &handle_tick,    // called repeatedly, each second
     .tick_units = SECOND_UNIT        // specifies interval of `tick_handler`
    },
    .timer_handler = &handle_timer
  };
  app_event_loop(params, &handlers);

}


