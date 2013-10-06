#include "pebble_os.h"
#include "pebble_app.h"
#include "pebble_fonts.h"
#include "http.h"
#include "mini-printf.h"
#define ISS_COOKIE 6878


#define MY_UUID { 0x6D, 0x76, 0x59, 0xAA, 0x74, 0x63, 0x40, 0x1E, 0xA9, 0xBE, 0x1E, 0x88, 0xE8, 0x65, 0xB8, 0xB5 }
PBL_APP_INFO(HTTP_UUID,
             "Balance", "Acme",
             1, 0, /* App version */
             DEFAULT_MENU_ICON,
             APP_INFO_STANDARD_APP);

Window window;
#define COOKIE_MY_TIMER 12121

TextLayer balanceDisp;
TextLayer amountDisp;
TextLayer transDisp;
TextLayer balanceNameDisp;

AppTimerHandle timer_handle;


int error = 0;
char str[20];
char *balance;


void window_appear(Window* me);
void httpebble_error(int error_code);
void select_single_click_handler(ClickRecognizerRef recognizer, Window *window);


void start_http_request() {

  DictionaryIterator *out;
  HTTPResult result;
  result = http_out_get("http://18.111.90.10:3000/balance", ISS_COOKIE, &out);

  if (result != HTTP_OK) {
    error = result;
    return;
  }
  result = http_out_send();
  if (result != HTTP_OK) {
    error = result;
    return;
  }
}


void config_provider(ClickConfig **config, Window *window) {
  // See ui/click.h for more information and default values.

  // single click / repeat-on-hold config:
  config[BUTTON_ID_SELECT]->click.handler = (ClickHandler) select_single_click_handler;
  config[BUTTON_ID_SELECT]->click.repeat_interval_ms = 1000; // "hold-to-repeat" gets overridden if there's a long click handler configured!

  (void)window;
}

void select_single_click_handler(ClickRecognizerRef recognizer, Window *window) {
  text_layer_set_text(&balanceDisp, "Loading..");

  start_http_request();
}

// Called when the http request is successful. Updates the nextpass_time.
void handle_http_success(int32_t request_id, int http_status, DictionaryIterator* sent, void* context) {
  if (balance != dict_find(sent, 2)->value->cstring) {
    vibes_long_pulse();
  }
  balance = dict_find(sent, 2)->value->cstring;
  text_layer_set_text(&amountDisp, dict_find(sent, 1)->value->cstring);
  text_layer_set_text(&balanceDisp, dict_find(sent, 2)->value->cstring);
  error = 0;
}

void handle_http_failure(int32_t request_id, int http_status, void* context) {
  mini_snprintf(str, 20, "Code: %d", http_status);
  text_layer_set_text(&balanceDisp, str);
}

void window_appear(Window* me) {
  start_http_request();
}

void handle_init(AppContextRef ctx) {
  http_set_app_id(34634645);

  window_init(&window, "Window Name");
  window_stack_push(&window, true /* Animated */);  
  window_set_window_handlers(&window, (WindowHandlers){
    .appear  = window_appear
  });
  text_layer_init(&balanceDisp, GRect(0, 100, 144, 30));
  text_layer_init(&balanceNameDisp, GRect(0, 75, 144, 30));
  text_layer_init(&transDisp, GRect(0, 25, 144, 30));
  text_layer_init(&amountDisp, GRect(0, 50, 144, 30));

  text_layer_set_text_alignment(&balanceDisp, GTextAlignmentCenter);
  text_layer_set_text_alignment(&balanceNameDisp, GTextAlignmentCenter);
  text_layer_set_text_alignment(&transDisp, GTextAlignmentCenter);
  text_layer_set_text_alignment(&amountDisp, GTextAlignmentCenter);

  text_layer_set_font(&balanceDisp, fonts_get_system_font(FONT_KEY_ROBOTO_CONDENSED_21));
  text_layer_set_font(&balanceNameDisp, fonts_get_system_font(FONT_KEY_GOTHIC_24_BOLD));
  text_layer_set_font(&transDisp, fonts_get_system_font(FONT_KEY_GOTHIC_24_BOLD));
  text_layer_set_font(&amountDisp, fonts_get_system_font(FONT_KEY_ROBOTO_CONDENSED_21));

  layer_add_child(&window.layer, &balanceDisp.layer);
  layer_add_child(&window.layer, &transDisp.layer);
  layer_add_child(&window.layer, &amountDisp.layer);
  layer_add_child(&window.layer, &balanceNameDisp.layer);

  text_layer_set_text(&transDisp, "Latest Cost");
  text_layer_set_text(&balanceNameDisp, "Balance");

  window_set_click_config_provider(&window, (ClickConfigProvider) config_provider);
  timer_handle = app_timer_send_event(ctx, 5000 /* milliseconds */, COOKIE_MY_TIMER);

}

void handle_timer(AppContextRef ctx, AppTimerHandle handle, uint32_t cookie) {

  if (cookie == COOKIE_MY_TIMER) {
      start_http_request();
      timer_handle = app_timer_send_event(ctx, 5000 /* milliseconds */, COOKIE_MY_TIMER);
  }
  // If you want the timer to run again you need to call `app_timer_send_event()`
  // again here.
}

void pbl_main(void *params) {


  PebbleAppHandlers handlers = {
    .init_handler = &handle_init,
    .messaging_info = {
      .buffer_sizes = {
        .inbound = 124,
        .outbound = 124,
      }
    },
    .timer_handler = &handle_timer
  };
  HTTPCallbacks http_callbacks = {
    .failure = handle_http_failure,
    .success = handle_http_success
  };
  http_register_callbacks(http_callbacks, NULL);
  app_event_loop(params, &handlers);

}


