package us.hourgeon.jmessenger.client;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.MultipleSelectionModel;

public class GroupSelectionModel<T> extends MultipleSelectionModel<T> {

    private ObservableList<T> items;
    private ObservableList<T> selected;
    private ObservableList<Integer> indices;

    private GroupSelectionModel<T> soulmate;

    /**
     * Constructor
     * @param items The list of items used by the ListView
     */
    GroupSelectionModel(ObservableList<T> items) {
        this.items = items;
        selected = FXCollections.observableArrayList();
        indices = FXCollections.observableArrayList();
    }


    /**
     * Set the object's soulmate. It will be used to synchronize selection
     * @param soulmate Another GroupSelectionModel
     */
    void setSoulmate(GroupSelectionModel<T> soulmate) {
        this.soulmate = soulmate;
    }

    @Override
    public void clearAndSelect(int i) {
        clearSelection(i);
        select(i);
    }

    @Override
    public void select(int i) {
        clearSelection();

        indices.add(i);
        selected.add(items.get(i));

        setSelectedIndex(i);
        setSelectedItem(items.get(i));

        soulmate.clearSelection();
    }

    @Override
    public void select(T t) {
        clearSelection();

        selected.add(t);
        indices.add(items.indexOf(t));

        setSelectedIndex(items.indexOf(t));
        setSelectedItem(t);

        soulmate.clearSelection();
    }

    @Override
    public void clearSelection(int i) {
        clearSelection();
    }

    @Override
    public void clearSelection() {
        indices.clear();
        selected.clear();
        setSelectedItem(soulmate.getSelectedItem());
    }

    @Override
    public boolean isSelected(int i) {
        return i >= 0 && i < items.size() && indices.contains(i);
    }

    @Override
    public boolean isEmpty() {
        return items.isEmpty();
    }

    @Override
    public void selectPrevious() {
        if (indices.get(0) > 0) {
            select(indices.get(0) - 1);
        }
    }

    @Override
    public void selectNext() {
        if (indices.get(0) < items.size() - 1) {
            select(indices.get(0) + 1);
        }
    }

    @Override
    public ObservableList<Integer> getSelectedIndices() {
        return indices;
    }

    @Override
    public ObservableList<T> getSelectedItems() {
        return selected;
    }

    @Override
    public void selectIndices(int i, int... ints) {
        select(i);
    }

    @Override
    public void selectAll() {
        clearSelection();
    }

    @Override
    public void selectFirst() {
        select(0);
    }

    @Override
    public void selectLast() {
        select(items.size() - 1);
    }
}
