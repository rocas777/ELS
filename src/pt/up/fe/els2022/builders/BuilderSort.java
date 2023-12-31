package pt.up.fe.els2022.builders;

import pt.up.fe.els2022.dslParser.CommandHolder;
import pt.up.fe.els2022.dslParser.Command;
import pt.up.fe.els2022.dslParser.commands.Sort;

public class BuilderSort implements InterfaceBuilder{
    Sort sort;
    CommandHolder builder;

    public BuilderSort(CommandHolder builder) {
        this.sort = new Sort();
        this.builder = builder;
    }

    public BuilderSort setCol(String col) {
        this.sort.setCol(col);
        return this;
    }
    public BuilderSort setFileId(String id) {
        this.sort.setFileId(id);
        return this;
    }
    public BuilderSort setNewFileId(String newFileId) {
        this.sort.setNewFileId(newFileId);
        return this;
    }
    public BuilderSort setDirection(String direction) {
        this.sort.setDirection(direction);
        return this;
    }

    public CommandHolder close(){
        return builder;
    }

    @Override
    public Command build() {
        return sort;
    }
}
