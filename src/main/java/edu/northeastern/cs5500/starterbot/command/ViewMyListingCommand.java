package edu.northeastern.cs5500.starterbot.command;

import edu.northeastern.cs5500.starterbot.controller.ListingController;
import edu.northeastern.cs5500.starterbot.controller.UserController;
import edu.northeastern.cs5500.starterbot.model.Listing;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.bson.types.ObjectId;

import com.mongodb.client.FindIterable;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.interactions.MessageEditCallbackAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

@Singleton
@Slf4j
public class ViewMyListingCommand implements SlashCommandHandler, ButtonHandler {

    private static final Integer EMBED_COLOR = 0x00FFFF;

    @Inject ListingController listingController;
    @Inject UserController userController;

    @Inject
    public ViewMyListingCommand() {
        // Defined public and empty for Dagger injection
    }

    @Override
    @Nonnull
    public String getName() {
        return "viewmylisting";
    }

    @Override
    @Nonnull
    public CommandData getCommandData() {
        return Commands.slash(getName(), "View all the listings that you have posted");
    }

    @Override
    public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {
        log.info("event: /viewmylisting");

        User user = event.getUser();

        FindIterable<Listing> listing = listingController.filterListingsByMembersId(user.getId());

        if (this.listingController.countListingsByMemberId(user.getId()) != 0) {
            for (Listing list: listing) {
                MessageCreateBuilder messageCreateBuilder = new MessageCreateBuilder();
                messageCreateBuilder =
                        messageCreateBuilder
                                .addActionRow(
                                        Button.danger(this.getName() + ":" + list.getMessageId().toString() + ":" + list.getId().toHexString() + ":delete", "Delete"))
                                .setEmbeds(this.listingController.toMessageEmbed(list));
                log.info("in for loop");
                event.getUser()
                .openPrivateChannel()
                .complete()
                .sendMessage(messageCreateBuilder.build())
                .queue();
            }
            event.reply("Your postings has been sent to your DM").setEphemeral(true).complete();
        } else {
            event.reply("No postings available").setEphemeral(true).complete();
        }
    }

    @Override
    public void onButtonInteraction(@Nonnull ButtonInteractionEvent event) {
        User user = event.getUser();
        String[] buttonIds = event.getButton().getId().split(":");
        this.listingController.deleteListingById(new ObjectId(buttonIds[2]));
        event.reply("Your post has been successfully deleted").complete();
    }
}
