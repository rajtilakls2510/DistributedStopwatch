package ui;

import java.util.ArrayList;

public class ListView<T> {

    /**
     * List Data Structure to store the items for remote virtual stopwatches
     */

    ArrayList<T> items;

    public ListView() {
        items = new ArrayList<>();
    }

    public void add(T item) {
        items.add(item);
    }

    public void remove(T item) {
        items.remove(item);
    }

    public T getItem(int index) {
        return items.get(index);
    }

}
