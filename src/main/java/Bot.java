import Entity.HistoryEntity;
import Entity.UptimeStatEntity;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.telegram.telegrambots.api.methods.BotApiMethod;
import org.telegram.telegrambots.api.methods.send.SendDocument;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.ForceReplyKeyboard;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.updateshandlers.SentCallback;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;


public class Bot extends TelegramLongPollingBot {
    private static Bot instance;
    private CommandHandler commandHandler;

    private long currentCommandId;
    private String currentCommand;

    private Bot(){
        this.commandHandler = new CommandHandler();
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
        Message recvdMsg = update.getMessage();
//        recvdMsg.getFrom().getUserName();
        System.out.println("Recv msg=" + recvdMsg.getMessageId());
        if (this.commandHandler.cmdWithArgs(recvdMsg.getText())){
            this.onCommandWithArgsRecvd(recvdMsg);
        } else if (recvdMsg.isReply() && recvdMsg.getReplyToMessage().getMessageId() == this.currentCommandId){
            this.executeCommandWithArgs(recvdMsg);
        } else {
            this.executeCommand(recvdMsg);
        }

    }

    /**
     * Метод для настройки сообщения и его отправки.
     * @param chatId id чата
     * @param s Строка, которую необходимот отправить в качестве сообщения.
     */
    public synchronized void sendMsg(String chatId, String s) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(s);
        try {
            sendMessage(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
            sendMessage.setText(e.toString());
            try {
                sendMessage(sendMessage);
            } catch (TelegramApiException ex) {
                ex.printStackTrace();
            }
        }
    }

    public synchronized void sendFile(String chatId, String path, String type) {
        if (type.equals("doc")) {
            SendDocument doc = new SendDocument();
            doc.setChatId(chatId);
            doc.setNewDocument(new File(path));
            try {
                sendDocument(doc);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
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

    public void onCommandWithArgsRecvd(Message msg) {
        this.currentCommand = msg.getText();
        SendMessage sendMessageRequest = new SendMessage();
        sendMessageRequest.setChatId(msg.getChatId());
        sendMessageRequest.setReplyToMessageId(msg.getMessageId());
        ForceReplyKeyboard forceReplyKeyboard = new ForceReplyKeyboard();
        forceReplyKeyboard.setSelective(true);
        sendMessageRequest.setReplyMarkup(forceReplyKeyboard);
        sendMessageRequest.setText("Введите аргументы команды:");

        try {
            executeAsync(sendMessageRequest, new SentCallback<Message>() {
                @Override
                public void onResult(BotApiMethod<Message> botApiMethod, Message sentMessage) {
                    if (sentMessage != null) {
                        currentCommandId = sentMessage.getMessageId();
                    }
                }

                @Override
                public void onError(BotApiMethod<Message> botApiMethod, TelegramApiRequestException e) {
                }

                @Override
                public void onException(BotApiMethod<Message> botApiMethod, Exception e) {
                }
            });
        } catch (TelegramApiException e) {
            System.out.println("Error");
        }
    }

    public void executeCommandWithArgs(Message args) {
        System.out.println("execut with args=" + args.getText());
        try {
            if (Commands.SEND_FILE.getCommandName().equals(this.currentCommand)){
                this.sendFile(args.getChatId().toString(), args.getText(), "doc");
            } else {
                String answer = this.commandHandler.processCommand(this.currentCommand, args.getText());
                this.sendMsg(args.getChatId().toString(), answer);
                this.saveDataToDatabase(this.currentCommand, answer, args.getFrom().getUserName());
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void executeCommand(Message cmd) {
        System.out.println("execut without args=" + cmd.getText());
        try {
            if (this.sendDataFromDatabase(cmd.getChatId().toString(), cmd.getText())) {
                return;
            }
            String answer = this.commandHandler.processCommand(cmd.getText(), "");
            this.sendMsg(cmd.getChatId().toString(), answer);
            this.saveDataToDatabase(cmd.getText(), answer, cmd.getFrom().getUserName());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private boolean sendDataFromDatabase(String chatId, String cmd) {
        if (Commands.UPTIME_STAT.getCommandName().equals(cmd)) {
            Session session = HibernateSessionFactory.getSessionFactory().openSession();
            Criteria criteria = session.createCriteria(UptimeStatEntity.class);
            List<UptimeStatEntity> utimes = criteria.list();
            StringBuilder builder = new StringBuilder();
            for (UptimeStatEntity utim : utimes) {
                builder.append(utim.getDate()).append(" | ");
                builder.append(utim.getValue());
                System.out.println(builder.toString());
            }
            this.sendMsg(chatId, builder.toString());
            return true;
        } else if (Commands.HISTORY.getCommandName().equals(cmd)) {
            Session session = HibernateSessionFactory.getSessionFactory().openSession();
            Criteria criteria = session.createCriteria(HistoryEntity.class);
            List<HistoryEntity> historyEntityList = criteria.list();
            StringBuilder builder = new StringBuilder();
            for (HistoryEntity item : historyEntityList) {
                builder.append(item.getId()).append(" | ");
                builder.append(item.getDate()).append(" | ");
                builder.append(item.getCommand()).append(" | ");
                builder.append(item.getUsername()).append("\n");
                System.out.println(builder.toString());
            }
            this.sendMsg(chatId, builder.toString());
            return true;
        }
        return false;
    }

    private void saveDataToDatabase(String cmd, String data, String username){
        Session session = HibernateSessionFactory.getSessionFactory().openSession();
        session.beginTransaction();
        if (Commands.UPTIME.getCommandName().equals(cmd)){
            UptimeStatEntity uptimeStatEntity = new UptimeStatEntity();
            uptimeStatEntity.setValue(data);
            Timestamp t = new Timestamp(System.currentTimeMillis());
            uptimeStatEntity.setDate(t);
            session.save(uptimeStatEntity);
        }
        System.out.println("Save history");
        HistoryEntity historyEntity = new HistoryEntity();
        historyEntity.setCommand(cmd);
        Timestamp t = new Timestamp(System.currentTimeMillis());
        historyEntity.setDate(t);
        historyEntity.setUsername(username);
        session.save(historyEntity);
        session.getTransaction().commit();
        session.close();

    }


}
