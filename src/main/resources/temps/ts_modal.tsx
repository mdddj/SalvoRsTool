import React from 'react';


const Dialog: React.FC<React.PropsWithChildren<{id: string,onClose?: ()=>void}>> = ({
                                                                                        children,
                                                                                        id,onClose
                                                                                    }) => {
    return <dialog id={id} className={'modal'} onClose={onClose}>
        {children}
    </dialog>;
};

const DialogTitle: React.FC<{ title: string }> = ({ title }) => {
    return <h2 className={'font-bold text-lg'}>{title}</h2>;
};

const DialogCloseBtn: React.FC = () => {
    return <form method={'dialog'} className={'modal-backdrop'}>
        <button>close</button>
    </form>;
};

const DialogBody: React.FC<React.PropsWithChildren> = ({ children }) => {
    return <div className={'modal-box flex gap-3 flex-col'}>
        {children}
    </div>;
};


const DialogActions: React.FC<React.PropsWithChildren> = ({children}) => {
    return <div className={'modal-action'}>{children}</div>
}

export {
    Dialog,
    DialogTitle,
    DialogCloseBtn,
    DialogBody,
    DialogActions
};