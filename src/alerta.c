#include "pebble_os.h"
#include "pebble_app.h"
#include "pebble_fonts.h"
#include "mini-printf.h"


#define MY_UUID { 0x6E, 0x76, 0x59, 0xAA, 0x77, 0x63, 0x40, 0x1E, 0xA9, 0xBE, 0x1E, 0x88, 0xE8, 0x65, 0xB8, 0xB5 }
PBL_APP_INFO(MY_UUID,
             "Alerta", "Acme",
             1, 0, /* App version */
             DEFAULT_MENU_ICON,
             APP_INFO_STANDARD_APP);

Window window;

TextLayer rapeText;

AppTimerHandle timer_handle;

BmpContainer image;

#define COOKIE_MY_TIMER 8989


char str[20];
static bool callbacks_registered;
static AppMessageCallbacksNode app_callbacks;

void select_single_click_handler(ClickRecognizerRef recognizer, Window *window);
void up_single_click_handler(ClickRecognizerRef recognizer, Window *window);
void down_single_click_handler(ClickRecognizerRef recognizer, Window *window);
static void send_cmd(uint8_t cmd);

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

void layer_update_callback(Layer *me, GContext* ctx) {

  // We make sure the dimensions of the GRect to draw into
  // are equal to the size of the bitmap--otherwise the image
  // will automatically tile. Which might be what *you* want.

  GRect destination = layer_get_frame(&image.layer.layer);

  destination.origin.y = 5;
  destination.origin.x = 5;

  graphics_draw_bitmap_in_rect(ctx, &image.bmp, destination);


  destination.origin.x = 80;
  destination.origin.y = 60;

  graphics_draw_bitmap_in_rect(ctx, &image.bmp, destination);
}


void select_single_click_handler(ClickRecognizerRef recognizer, Window *window) {
  text_layer_set_text(&rapeText, "Sending Message");
  send_cmd(0x01);
}

void up_single_click_handler(ClickRecognizerRef recognizer, Window *window) {


}

void down_single_click_handler(ClickRecognizerRef recognizer, Window *window) {

}

void handle_timer(AppContextRef ctx, AppTimerHandle handle, uint32_t cookie) {

 
}

static void app_send_failed(DictionaryIterator* failed, AppMessageResult reason, void* context) {
  // TODO: error handling
    text_layer_set_text(&rapeText, "Message Failure");

}

static void app_received_msg(DictionaryIterator* received, void* context) {
  text_layer_set_text(&rapeText, "Alert Sent Out");
  vibes_double_pulse();
}

bool register_callbacks() {
  if (callbacks_registered) {
    if (app_message_deregister_callbacks(&app_callbacks) == APP_MSG_OK)
      callbacks_registered = false;
  }
  if (!callbacks_registered) {
    app_callbacks = (AppMessageCallbacksNode){
      .callbacks = {
        .out_failed = app_send_failed,
        .in_received = app_received_msg
      },
      .context = NULL
    };
    if (app_message_register_callbacks(&app_callbacks) == APP_MSG_OK) {
      callbacks_registered = true;
    }
  }
  return callbacks_registered;
}

static void send_cmd(uint8_t cmd) {
  Tuplet value = TupletInteger(0, cmd);
  
  DictionaryIterator *iter;
  app_message_out_get(&iter);
  
  if (iter == NULL)
    return;
  
  dict_write_tuplet(iter, &value);
  dict_write_end(iter);
  
  app_message_out_send();
  app_message_out_release();
}
void handle_init(AppContextRef ctx) {

  window_init(&window, "Window Name");
  window_stack_push(&window, true /* Animated */);  
  text_layer_init(&rapeText, GRect(0, 85, 144, 30));
  text_layer_set_text_alignment(&rapeText, GTextAlignmentCenter);
  text_layer_set_font(&rapeText, fonts_get_system_font(FONT_KEY_ROBOTO_CONDENSED_21));
  layer_add_child(&window.layer, &rapeText.layer);
  text_layer_set_text(&rapeText, "Press if Raped lol");
  window_set_click_config_provider(&window, (ClickConfigProvider) config_provider);
  
  resource_init_current_app(&APP_RESOURCES);
  bmp_init_container(RESOURCE_ID_ALERTA_ICON, &image);
  layer_add_child(&window.layer, &image.layer.layer);
  register_callbacks();

}

void handle_tick(AppContextRef ctx, PebbleTickEvent *event) {
 
}

void handle_deinit(AppContextRef ctx) {

  bmp_deinit_container(&image);
}


void pbl_main(void *params) {

  PebbleAppHandlers handlers = {
    .init_handler = &handle_init,
    .deinit_handler = &handle_deinit,
    .tick_info = {
     .tick_handler = &handle_tick,    // called repeatedly, each second
     .tick_units = SECOND_UNIT        // specifies interval of `tick_handler`
    },
    .messaging_info = {
      .buffer_sizes = {
        .inbound = 256,
        .outbound = 256,
      }
    } 
  };
  app_event_loop(params, &handlers);

}


