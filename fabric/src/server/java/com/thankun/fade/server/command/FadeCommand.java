package com.thankun.fade.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.thankun.fade.server.config.FadeServerConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class FadeCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        
        LiteralArgumentBuilder<CommandSourceStack> fadeCommand = Commands.literal("fade")
            .requires(source -> {
                ServerPlayer player = source.getPlayer();
                return player != null ? source.getServer().getPlayerList().isOp(player.nameAndId()) : true;
            });
        LiteralArgumentBuilder<CommandSourceStack> configNode = Commands.literal("config");
        RequiredArgumentBuilder<CommandSourceStack, Integer> distanceArg = Commands.argument("value", IntegerArgumentType.integer(1, 128))
            .executes(context -> {
                int distance = IntegerArgumentType.getInteger(context, "value");
                FadeServerConfig.setPlayerCullDistance(distance);
                boolean isEn = "en_us".equalsIgnoreCase(FadeServerConfig.lang);
                String msg = isEn ? "[Fade Server] Culling distance has been saved and set to " + distance + " blocks." : "[Fade Server] 작동 기준 거리가 " + distance + "블록으로 저장 및 커스텀 설정되었습니다.";
                context.getSource().sendSuccess(() -> Component.literal(msg), true);
                return 1;
            });
        configNode.then(Commands.literal("distance").then(distanceArg));
        RequiredArgumentBuilder<CommandSourceStack, String> targetArg = Commands.argument("value", StringArgumentType.word())
            .suggests((context, builder) -> SharedSuggestionProvider.suggest(new String[]{"PLAYERS_ONLY", "ALL", "ENTITIES_ONLY"}, builder))
            .executes(context -> {
                String target = StringArgumentType.getString(context, "value").toUpperCase();
                boolean isEn = "en_us".equalsIgnoreCase(FadeServerConfig.lang);
                if (!target.equals("PLAYERS_ONLY") && !target.equals("ALL") && !target.equals("ENTITIES_ONLY")) {
                    String failMsg = isEn ? "[Fade Server] Invalid target name. (Choose from PLAYERS_ONLY, ALL, ENTITIES_ONLY)" : "[Fade Server] 잘못된 타겟 명칭입니다. (PLAYERS_ONLY, ALL, ENTITIES_ONLY 중 입력)";
                    context.getSource().sendFailure(Component.literal(failMsg));
                } else {
                    FadeServerConfig.setCullTarget(target);
                    String msg = isEn ? "[Fade Server] Culling target has been saved and set to " + target + "." : "[Fade Server] 작동 타겟이 " + target + "(으)로 저장 및 커스텀 설정되었습니다.";
                    context.getSource().sendSuccess(() -> Component.literal(msg), true);
                }
                return 1;
            });
        configNode.then(Commands.literal("target").then(targetArg));

        RequiredArgumentBuilder<CommandSourceStack, String> modeArg = Commands.argument("value", StringArgumentType.word())
            .suggests((context, builder) -> SharedSuggestionProvider.suggest(new String[]{"VANISH"}, builder))
            .executes(context -> {
                String mode = StringArgumentType.getString(context, "value").toUpperCase();
                boolean isEn = "en_us".equalsIgnoreCase(FadeServerConfig.lang);
                if (mode.equals("FADE")) {
                    String denyMsg = isEn ? "[Fade Server] FADE mode is not supported on this server setup." : "[Fade Server] 해당 서버 팩 사양에서는 FADE 모드를 지원하지 않습니다! (VANISH 모드만 가능)";
                    context.getSource().sendFailure(Component.literal(denyMsg));
                    return 0; 
                }
                
                if (mode.equals("VANISH")) {
                    FadeServerConfig.setCullMode(mode);
                    String msg = isEn ? "[Fade Server] Culling render mode has been saved and set to " + mode + "!" : "[Fade Server] 차단 연출 모드가 " + mode + "(으)로 저장 및 커스텀 설정되었습니다!";
                    context.getSource().sendSuccess(() -> Component.literal(msg), true);
                } else {
                    String failMsg = isEn ? "[Fade Server] Invalid render mode. (Choose: VANISH)" : "[Fade Server] 잘못된 연출 모드입니다. (VANISH 모드만 사용 가능)";
                    context.getSource().sendFailure(Component.literal(failMsg));
                }
                return 1;
            });
        configNode.then(Commands.literal("mode").then(modeArg));
        RequiredArgumentBuilder<CommandSourceStack, String> operationArg = Commands.argument("value", StringArgumentType.word())
            .suggests((context, builder) -> SharedSuggestionProvider.suggest(new String[]{"OPTIMIZE", "FADE_OUT"}, builder))
            .executes(context -> {
                String opMode = StringArgumentType.getString(context, "value").toUpperCase();
                boolean isEn = "en_us".equalsIgnoreCase(FadeServerConfig.lang);
                if (!opMode.equals("OPTIMIZE") && !opMode.equals("FADE_OUT")) {
                    String failMsg = isEn ? "[Fade Server] Invalid operation mode. (Choose from OPTIMIZE, FADE_OUT)" : "[Fade Server] 잘못된 모드입니다. (OPTIMIZE[멀면숨김], FADE_OUT[가까우면숨김] 중 입력)";
                    context.getSource().sendFailure(Component.literal(failMsg));
                } else {
                    FadeServerConfig.setOperationMode(opMode);
                    String msg = isEn ? "[Fade Server] Operation mode has been saved and changed to " + opMode + " in real-time!" : "[Fade Server] 작동 조건이 " + opMode + " 모드로 저장 및 실시간 전환되었습니다!";
                    context.getSource().sendSuccess(() -> Component.literal(msg), true);
                }
                return 1;
            });
        configNode.then(Commands.literal("operation").then(operationArg));

        RequiredArgumentBuilder<CommandSourceStack, String> langArg = Commands.argument("value", StringArgumentType.word())
            .suggests((context, builder) -> SharedSuggestionProvider.suggest(new String[]{"ko_kr", "en_us"}, builder))
            .executes(context -> {
                String inputLang = StringArgumentType.getString(context, "value").toLowerCase();
                if (!inputLang.equals("ko_kr") && !inputLang.equals("en_us")) {
                    boolean isEn = "en_us".equalsIgnoreCase(FadeServerConfig.lang);
                    String failMsg = isEn ? "[Fade Server] Unsupported language. (Available: ko_kr, en_us)" : "[Fade Server] 지원하지 않는 언어입니다. (지원 가능: ko_kr, en_us)";
                    context.getSource().sendFailure(Component.literal(failMsg));
                } else {
                    FadeServerConfig.lang = inputLang;
                    FadeServerConfig.save();
                    boolean isEn = "en_us".equalsIgnoreCase(FadeServerConfig.lang);
                    String msg = isEn ? "[Fade Server] Language changed to English (en_us) and config file updated." : "[Fade Server] 언어가 한국어(ko_kr)로 변경되었으며 설정 파일이 동기화되었습니다.";
                    context.getSource().sendSuccess(() -> Component.literal(msg), true);
                }
                return 1;
            });
        configNode.then(Commands.literal("lang").then(langArg));
        RequiredArgumentBuilder<CommandSourceStack, Boolean> fallingBlocksArg = Commands.argument("value", BoolArgumentType.bool())
            .executes(context -> {
                boolean allowed = BoolArgumentType.getBool(context, "value");
                FadeServerConfig.setCullFallingBlocks(allowed);
                boolean isEn = "en_us".equalsIgnoreCase(FadeServerConfig.lang);
                String msg = isEn ? "[Fade Server] Cull falling blocks setting has been set to " + allowed + "." : "[Fade Server] 떨어지는 블록의 최적화 차단 여부가 " + allowed + "(으)로 설정 및 저장되었습니다.";
                context.getSource().sendSuccess(() -> Component.literal(msg), true);
                return 1;
            });
        configNode.then(Commands.literal("fallingblocks").then(fallingBlocksArg));
        RequiredArgumentBuilder<CommandSourceStack, Boolean> fadeItemVanishArg = Commands.argument("value", BoolArgumentType.bool())
            .executes(context -> {
                boolean enabled = BoolArgumentType.getBool(context, "value");
                FadeServerConfig.setFadeItemAsVanish(enabled);
                boolean isEn = "en_us".equalsIgnoreCase(FadeServerConfig.lang);
                String msg = isEn ? "[Fade Server] Fade item as vanish mode has been set to " + enabled + "." : "[Fade Server] 페이드 상태 시 아이템 증발(소멸) 모드가 " + enabled + "(으)로 원격 제어 세팅되었습니다.";
                context.getSource().sendSuccess(() -> Component.literal(msg), true);
                return 1;
            });
        configNode.then(Commands.literal("fadeitemvanish").then(fadeItemVanishArg));

        RequiredArgumentBuilder<CommandSourceStack, String> filterArg = Commands.argument("value", StringArgumentType.greedyString())
            .executes(context -> {
                String filterInput = StringArgumentType.getString(context, "value");
                FadeServerConfig.setCustomFilterString(filterInput);
                boolean isEn = "en_us".equalsIgnoreCase(FadeServerConfig.lang);
                String msg = isEn ? "[Fade Server] Custom target filter updated: " + filterInput : "[Fade Server] 커스텀 타겟 필터 문자열이 성공적으로 업데이트되었습니다: " + filterInput;
                context.getSource().sendSuccess(() -> Component.literal(msg), true);
                return 1;
            });
        configNode.then(Commands.literal("filter").then(filterArg));
        fadeCommand.then(configNode);
        dispatcher.register(fadeCommand);
    }
}