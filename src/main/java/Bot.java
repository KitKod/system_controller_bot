import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.io.IOException;
import java.util.logging.Level;

public class Bot extends TelegramLongPollingBot {
    private static Bot instance;
    private DefaultCommand defCommand;

    private Bot(){
        this.defCommand = new DefaultCommand();
    }

    public static Bot getBot() {
        if (instance == null) {
            instance = new Bot();
        }
        return instance;
    }

    /**
     * Метод для приема сообщений.
     * @param update Содержит сообщение от пользователя.
     */
    @Override
    public void onUpdateReceived(Update update) {
        String message = update.getMessage().getText();
        String ChatID = update.getMessage().getChatId().toString();
        if (this.defCommand.isBelong(message)){
            String answer = null;
            try {
                answer = this.defCommand.processCommand(message);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            sendMsg(ChatID, answer);
        } else {
            sendMsg(ChatID, message);
        }

    }

    /**
     * Метод для настройки сообщения и его отправки.
     * @param chatId id чата
     * @param s Строка, которую необходимот отправить в качестве сообщения.
     */
    public synchronized void sendMsg(String chatId, String s) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(chatId);
        sendMessage.setText(s);
        try {
            sendMessage(sendMessage);
        } catch (TelegramApiException e) {

        }
    }

    /**
     * Метод возвращает имя бота, указанное при регистрации.
     * @return имя бота
     */
    @Override
    public String getBotUsername() {
        return "system_controller_bot";
    }

    /**
     * Метод возвращает token бота для связи с сервером Telegram
     * @return token для бота
     */
    @Override
    public String getBotToken() {
        return "914660109:AAFGdaFvef5WOPmSSiYv5_caZz2zZKlVrho";
    }
}
