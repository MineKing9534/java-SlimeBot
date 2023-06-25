package com.slimebot.report.commands;

import com.slimebot.main.Main;
import com.slimebot.report.assets.Report;
import com.slimebot.report.assets.Status;
import com.slimebot.utils.Checks;
import com.slimebot.utils.Config;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import org.simpleyaml.configuration.ConfigurationSection;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;

public class ReportList extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        super.onSlashCommandInteraction(event);

        if (!(event.getName().equals("report_list"))) {return;}
        if (Checks.hasTeamRole(event.getMember(), event.getGuild())){
            EmbedBuilder noTeam = new EmbedBuilder()
                    .setTimestamp(LocalDateTime.now().atZone(ZoneId.systemDefault()))
                    .setColor(Main.embedColor(event.getGuild().getId()))
                    .setTitle(":exclamation: Error")
                    .setDescription("Der Befehl kann nur von einem Teammitglied ausgeführt werden!");
            event.replyEmbeds(noTeam.build()).setEphemeral(true).queue();
            return;
        }

        EmbedBuilder embed = new EmbedBuilder()
                .setTimestamp(LocalDateTime.now().atZone(ZoneId.systemDefault()))
                .setDescription("Nutze /report_detail oder das Dropdown menu um mehr infos zu einem Report zu bekommen.")
                .setColor(Main.embedColor(event.getGuild().getId()));

        ArrayList<Integer> ReportIdList = new ArrayList<>();
        int fieldSize = 0;
        boolean maxFieldSize = false;

        YamlFile reportFile = Config.getConfig(event.getGuild().getId(), "reports");
        try {
            reportFile.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ConfigurationSection reportSection = reportFile.getConfigurationSection("reports");
        ArrayList<Report> allReports = new ArrayList<>();
        for (int id = 2; id <= reportSection.size() ; id++) {
            allReports.add(Report.get(event.getGuild().getId(), id));
        }

        switch (event.getOption("status").getAsString()){
            case "all" -> {
                embed.setTitle("Eine Liste aller Reports");
                for (Report report:allReports) {
                    ReportIdList.add(report.getId());
                    if (fieldSize > 24){maxFieldSize = true; break;}
                    addReportField(report, embed);
                    fieldSize ++;
                }
            }
            case "closed" -> {
                embed.setTitle("Eine Liste aller geschlossenen Reports");
                for (Report report:allReports) {
                    if (!(report.status == Status.CLOSED)){continue; }
                    ReportIdList.add(report.getId());
                    if (fieldSize > 24){maxFieldSize = true;break;}
                    addReportField(report, embed);
                    fieldSize ++;
                }
            }
            case "open" -> {
                embed.setTitle("Eine Liste aller offenen Reports");
                for (Report report:allReports) {
                    if (!(report.status == Status.OPEN)){continue; }
                    ReportIdList.add(report.getId());
                    if (fieldSize > 24){maxFieldSize = true;break;}
                    addReportField(report, embed);
                    fieldSize ++;
                }
            }

        }

        if (ReportIdList.size() == 0){

            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setTimestamp(LocalDateTime.now().atZone(ZoneId.systemDefault()))
                    .setColor(Main.embedColor(event.getGuild().getId()))
                    .setTitle(":exclamation: Error: No Reports Found")
                    .setDescription("Es wurden keine Reports zu der Ausgewählten option (" + event.getOption("status").getAsString() + ") gefunden!");
            event.replyEmbeds(embedBuilder.build()).setEphemeral(true).queue();
            return;
        }

        if (maxFieldSize){
            embed.setFooter("ERROR Weitere Reports gefunden - Fehler beim laden - com.slimebot.report.commands.ReportList:86");
        }

        MessageEmbed ed = embed.build();

        event.replyEmbeds(ed).addActionRow(DetailDropdownButton(ReportIdList)).queue();



    }

    private void addReportField(Report report, EmbedBuilder embed){
        embed.addField("Report #" + report.getId().toString(),
                report.getUser().getAsMention() + " wurde am ` " + report.getTime().format(Main.dtf) + "` von " + report.getBy().getAsMention() + " gemeldet.",
                false);
    }

    private StringSelectMenu DetailDropdownButton(ArrayList<Integer> reportList){
        StringSelectMenu.Builder btnBuilder = StringSelectMenu.create("detail_btn")
                .setPlaceholder("Details zu einem Report")
                .setMaxValues(1);

        for (Integer reportID:reportList) {
            btnBuilder.addOption("Report #" + reportID, reportID.toString(), "Details zum Report #" + reportID);
        }

        return btnBuilder.build();

    }






}