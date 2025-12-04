
public class Machine {
    private int machineId;
    private String name;
    private String type;
    private MachineStatus status;

    public Machine() {}

    public Machine(int machineId, String name, String type, MachineStatus status) {
        this.machineId = machineId;
        this.name = name;
        this.type = type;
        this.status = status;
    }

    public int getMachineId() {
        return machineId;
    }

    public void setMachineId(int machineId) {
        this.machineId = machineId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public MachineStatus getStatus() {
        return status;
    }

    public void setStatus(MachineStatus status) {
        this.status = status;
    }
}


