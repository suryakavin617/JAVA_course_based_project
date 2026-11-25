package com.polarlog.model;

import com.polarlog.util.DateUtil;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/** A planned trip with its vessel, crew and cargo. */
public class Expedition implements Identifiable, Reportable, Serializable {
    private static final long serialVersionUID = 1L;

    private final int id;
    private final String name;
    private final String vessel;
    private final LocalDate departureDate;
    private final List<StationMember> crew = new ArrayList<>();
    private final List<Asset> cargo = new ArrayList<>();

    public Expedition(int id, String name, String vessel, LocalDate departureDate) {
        this.id = id;
        this.name = name;
        this.vessel = vessel;
        this.departureDate = departureDate;
    }

    @Override
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void addCrew(StationMember member) {
        if (crew.contains(member)) {
            throw new IllegalStateException(member.getName() + " is already in this expedition");
        }
        crew.add(member);
    }

    public void addCargo(Asset asset) {
        if (cargo.contains(asset)) {
            throw new IllegalStateException("Asset #" + asset.getId() + " is already in this expedition");
        }
        cargo.add(asset);
    }

    public long daysToDeparture() {
        return ChronoUnit.DAYS.between(LocalDate.now(), departureDate);
    }

    @Override
    public String report() {
        StringBuffer sb = new StringBuffer();
        String nl = System.lineSeparator();
        sb.append(reportHeader()).append(nl);
        sb.append("ID         : ").append(id).append(nl);
        sb.append("Name       : ").append(name).append(nl);
        sb.append("Vessel     : ").append(vessel).append(nl);
        sb.append("Departure  : ").append(DateUtil.format(departureDate))
          .append(" (in ").append(daysToDeparture()).append(" days)").append(nl);
        sb.append("Crew       : ");
        if (crew.isEmpty()) {
            sb.append("none");
        }
        for (StationMember m : crew) {
            sb.append(m.getName()).append("; ");
        }
        sb.append(nl).append("Cargo      : ");
        if (cargo.isEmpty()) {
            sb.append("none");
        }
        for (Asset a : cargo) {
            sb.append("#").append(a.getId()).append(" ").append(a.getName()).append("; ");
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return "#" + id + " " + name + " (" + vessel + ", departs " + DateUtil.format(departureDate)
            + ", crew=" + crew.size() + ", cargo=" + cargo.size() + ")";
    }
}
