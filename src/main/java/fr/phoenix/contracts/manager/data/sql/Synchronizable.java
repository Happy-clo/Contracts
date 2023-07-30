package fr.phoenix.contracts.manager.data.sql;

public interface Synchronizable {

    /**
     * This method enables synchronization between different BungeeCord servers by saving the data to SQL
     * when a change has been made and notifies all the other servers about this change.
     */
    void synchronize();
}
