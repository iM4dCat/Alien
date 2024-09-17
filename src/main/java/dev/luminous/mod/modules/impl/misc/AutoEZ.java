package dev.luminous.mod.modules.impl.misc;

import dev.luminous.Alien;
import dev.luminous.api.events.eventbus.EventHandler;
import dev.luminous.api.events.impl.DeathEvent;
import dev.luminous.mod.modules.Module;
import dev.luminous.mod.modules.settings.impl.EnumSetting;
import dev.luminous.mod.modules.settings.impl.SliderSetting;
import dev.luminous.mod.modules.settings.impl.StringSetting;
import net.minecraft.entity.player.PlayerEntity;

import java.util.List;
import java.util.Random;

public class AutoEZ extends Module {
    public enum Type {
        Bot,
        Custom,
        AutoSex
    }
    private final EnumSetting<Type> type = add(new EnumSetting<>("Type", Type.Bot));
    private final SliderSetting range =
            add(new SliderSetting("Range", 10, 0, 20,.1));
    private final StringSetting message = add(new StringSetting("Message", "EZ %player%", () -> type.getValue() == Type.Custom));
    private final SliderSetting randoms =
            add(new SliderSetting("Random", 3, 0, 20,1));
    public AutoEZ() {
        super("AutoEZ", Category.Misc);
        setChinese("自动嘲讽");
    }
    public List<String> sex = List.of("呐呐~杂鱼哥哥不会这样就被捉弄的不会说话了吧♡",
            "嘻嘻~杂鱼哥哥不会以为竖个大拇哥就能欺负我了吧~不会吧♡不会吧♡",
            "杂鱼哥哥怎么可能欺负得了别人呢~只能欺负自己哦♡~",
            "哥哥真是好欺负啊♡嘻嘻~",
            "哎♡~杂鱼说话就是无趣唉~",
            "呐呐~杂鱼哥哥发这个是想教育我吗~嘻嘻~怎么可能啊♡",
            "什么嘛~废柴哥哥会想这种事情啊~唔呃",
            "把你肮脏的目光拿开啦~很恶心哦♡",
            "咱的期待就是被你这样的笨蛋破坏了~♡");

    public List<String> bot = List.of("鼠标明天到，触摸板打的",
            "转人工",
            "收徒",
            "不收徒",
            "有真人吗",
            "墨镜上车",
            "素材局",
            "不接单",
            "接单",
            "征婚",
            "4399?",
            "暂时不考虑打职业",
            "bot?",
            "叫你家大人来打",
            "假肢上门安装",
            "浪费我的网费",
            "不收残疾人",
            "下课",
            "自己找差距",
            "不接代",
            "代+",
            "这样的治好了也流口水",
            "人机",
            "人机怎么调难度啊",
            "只收不被0封的",
            "Bot吗这是",
            "领养",
            "纳亲",
            "正视差距",
            "近亲繁殖?",
            "我玩的是新手教程?",
            "来调灵敏度的",
            "来调参数的",
            "小号",
            "不是本人别加",
            "下次记得晚点玩",
            "随便玩玩,不带妹",
            "扣1上车");

    Random random = new Random();
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    @EventHandler
    public void onDeath(DeathEvent event) {
        PlayerEntity player = event.getPlayer();
        if (player != mc.player && !Alien.FRIEND.isFriend(player)) {
            if (range.getValue() > 0 && mc.player.distanceTo(player) > range.getValue()) {
                return;
            }
            String randomString = generateRandomString(randoms.getValueInt());
            if (!randomString.isEmpty()) {
                randomString = " " + randomString;
            }
            switch (type.getValue())  {
                case Bot -> mc.getNetworkHandler().sendChatMessage(bot.get(random.nextInt(bot.size() - 1)) + " " + player.getName().getString() + randomString);
                case Custom -> mc.getNetworkHandler().sendChatMessage(message.getValue().replaceAll("%player%", player.getName().getString()) + randomString);
                case AutoSex -> mc.getNetworkHandler().sendChatMessage(sex.get(random.nextInt(sex.size() - 1)) + " " + player.getName().getString() + randomString);
            }
        }
    }

    private String generateRandomString(int LENGTH) {
        StringBuilder sb = new StringBuilder(LENGTH);

        for (int i = 0; i < LENGTH; i++) {
            int index = random.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(index));
        }

        return sb.toString();
    }
}